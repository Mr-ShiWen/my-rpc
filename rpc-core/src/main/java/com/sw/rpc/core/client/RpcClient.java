package com.sw.rpc.core.client;

import com.sw.rpc.assist.loadbalance.AbstractLoadBalance;
import com.sw.rpc.assist.loadbalance.LoadBalance;
import com.sw.rpc.assist.loadbalance.impl.ConsistentLoadBalance;
import com.sw.rpc.assist.loadbalance.impl.PollingLoadBalance;
import com.sw.rpc.assist.loadbalance.impl.RandomLoadBalance;
import com.sw.rpc.assist.registry.ServiceDiscover;
import com.sw.rpc.assist.registry.impl.ServiceDiscoverImpl;
import com.sw.rpc.config.LocalConfig;
import com.sw.rpc.dto.detail.RpcRequest;
import com.sw.rpc.dto.detail.RpcResponse;
import com.sw.rpc.netty.handler.RpcCodec;
import com.sw.rpc.netty.handler.RpcResponseHandler;
import com.sw.rpc.utils.UuidUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.sw.rpc.assist.loadbalance.AbstractLoadBalance.LoadBalanceType.*;
import static com.sw.rpc.constant.Constant.*;
import static com.sw.rpc.utils.ReflectionUtil.getNamesForClasses;

@Slf4j
public class RpcClient {
    /**
     * 存放映射 < requestId，resp >，方便获取对应请求的结果
     */
    public static ConcurrentHashMap<String, Promise<RpcResponse>> respMap = new ConcurrentHashMap<>();
    private ServiceDiscover serviceDiscover;
    private LoadBalance loadBalance;
    /**
     * 存放映射 <url,channel>，这些 channel 都是通过 boostrap 建立好的
     */
    private ConcurrentHashMap<String, NioSocketChannel> channelMap;
    private Bootstrap bootstrap;
    private int workerNum;

    /**
     * 服务发现模式的 rpc
     *
     * @param serverIntefaceClazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getServerProxyWithServiceDiscover(Class<T> serverIntefaceClazz, String... implName) {
        if (serviceDiscover == null) {
            throw new RuntimeException("There is no serverDiscover, please check zookeeper config and zookeeper server");
        }
        return (T) Proxy.newProxyInstance(
                serverIntefaceClazz.getClassLoader(),
                new Class[]{serverIntefaceClazz},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        // 1.构造 rpcRequest
                        RpcRequest rpcRequest = new RpcRequest(
                                UuidUtil.getRequestId(),
                                serverIntefaceClazz.getName(),
                                method.getName(),
                                getNamesForClasses(method.getParameterTypes()),
                                method.getReturnType().getName(),
                                args,
                                implName.length > 0 ? implName[0] : ""
                        );
                        // 2.调用得到 rpcResponse
                        RpcResponse rpcResponse = send(rpcRequest);
                        // 3.处理得到结果
                        if (rpcResponse.getErrMsg() == null) {
                            return rpcResponse.getReturnValue();
                        } else {
                            throw new RuntimeException(rpcResponse.getErrMsg());
                        }
                    }
                }
        );
    }

    @SuppressWarnings("unchecked")
    public <T> T getServerProxyWithSpecificConn(Class<T> serverIntefaceClazz, String serverIp, int serverPort, String... implName) {
        return (T) Proxy.newProxyInstance(
                serverIntefaceClazz.getClassLoader(),
                new Class[]{serverIntefaceClazz},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 1.构造 rpcRequest
                        RpcRequest rpcRequest = new RpcRequest(
                                UuidUtil.getRequestId(),
                                serverIntefaceClazz.getName(),
                                method.getName(),
                                getNamesForClasses(method.getParameterTypes()),
                                method.getReturnType().getName(),
                                args,
                                implName.length > 0 ? implName[0] : ""
                        );
                        // 2.调用得到 rpcResponse
                        RpcResponse rpcResponse = sendWithSpecificConn(rpcRequest, new InetSocketAddress(serverIp, serverPort));
                        // 3.处理得到结果
                        if (rpcResponse.getErrMsg() == null) {
                            return rpcResponse.getReturnValue();
                        } else {
                            throw new RuntimeException(rpcResponse.getErrMsg());
                        }
                    }
                }
        );
    }


    private RpcResponse send(RpcRequest rpcRequest) {
        try {
            // 0. 检查服务发现是否存在
            if (serviceDiscover == null) {
                throw new Exception("serviceDiscover does not exist, please check whether zookeeper works well");
            }

            // 1.服务发现
            List<String> urlList = serviceDiscover.discoverService(rpcRequest);

            // 2.负载均衡
            String address = loadBalance.selectServiceAddress(urlList, rpcRequest);

            // 3.解析得到地址
            String[] splits = address.split(":");
            InetSocketAddress inetSocketAddress = new InetSocketAddress(splits[0], Integer.valueOf(splits[1]));

            // 4.寻找 channel
            NioSocketChannel channel = channelMap.getOrDefault(inetSocketAddress.toString(), null);
            channel = channel == null ? buidChannel(inetSocketAddress) : channel;

            // 5.发送请求
            Promise<RpcResponse> respPromise = new DefaultPromise<>(new NioEventLoopGroup().next());
            respMap.put(rpcRequest.getRequestId(), respPromise);
            channel.writeAndFlush(rpcRequest);

            // 6. 同步等待结果
            RpcResponse rpcResponse = respPromise.get();

            // 7. 从 map 中删除结果
            respMap.remove(rpcRequest.getRequestId());

            // 8.返回结果
            return rpcResponse;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private RpcResponse sendWithSpecificConn(RpcRequest rpcRequest, InetSocketAddress inetSocketAddress) {
        try {
            // 1.寻找 channel
            NioSocketChannel channel = channelMap.getOrDefault(inetSocketAddress.toString(), null);
            channel = channel == null ? buidChannel(inetSocketAddress) : channel;

            // 2.发送请求
            Promise<RpcResponse> respPromise = new DefaultPromise<>(new NioEventLoopGroup().next());
            respMap.put(rpcRequest.getRequestId(), respPromise);
            channel.writeAndFlush(rpcRequest);

            // 3. 同步等待结果
            RpcResponse rpcResponse = respPromise.get();

            // 4. 从 map 中删除结果
            respMap.remove(rpcRequest.getRequestId());

            // 5.返回结果
            return rpcResponse;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private NioSocketChannel buidChannel(InetSocketAddress inetSocketAddress) {
        try {
            // 同步建立连接
            ChannelFuture channelFuture = bootstrap.connect(inetSocketAddress).sync();
            NioSocketChannel channel = (NioSocketChannel) channelFuture.channel();
            channelMap.put(inetSocketAddress.toString(), channel);
            return channel;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public RpcClient(AbstractLoadBalance.LoadBalanceType loadBalanceType) {
        initClient(loadBalanceType);
    }

    public RpcClient() {
        AbstractLoadBalance.LoadBalanceType loadBalanceType;
        // 读取配置文件，得到注册中心 zookeeper 的配置
        String loadBalanceProperty = LocalConfig.getProperty("rpc.client.loadBalance");
        switch (loadBalanceProperty) {
            case "rolling":
                initClient(LOADBALANCETYPE_POLLING);
                break;
            case "random":
                initClient(LOADBALANCETYPE_RANDOM);
                break;
            case "consistent":
                initClient(LOADBALANCETYPE_CONSISTENT);
                break;
            default:
                initClient(LOADBALANCETYPE_POLLING);
        }
    }

    private void initClient(AbstractLoadBalance.LoadBalanceType loadBalanceType) {
        // 1.初始化服务发现
        try {
            // 读取 zookeeper 的集群 ip
            String connections = LocalConfig.getProperty("zookeeper.connections");

            // 读取 zookeeper 的连接超时配置
            String timeoutProperty = LocalConfig.getProperty("zookeeper.sessionTimeOut");
            int sessionTimeOut = timeoutProperty == null ? ZOOKEEPER_SESSION_TIME_OUT : Integer.valueOf(LocalConfig.getProperty("zookeeper.sessionTimeOut"));

            // 读取 zookeeper 的 rpcRootPath
            String rpcRootPath = LocalConfig.getProperty("zookeeper.rpcRootPath");
            rpcRootPath = rpcRootPath == null ? RPC_ROOT_PATH : rpcRootPath;

            // 初始化服务发现
            serviceDiscover = new ServiceDiscoverImpl(connections, sessionTimeOut, rpcRootPath);
            log.info("init serviceDiscover successfully");
        } catch (Exception e) {
            // 没有服务发现不会抛出异常，走非服务发现模式
            log.error("init serviceDiscover fail");
            e.printStackTrace();
        }

        // 2.初始化负载均衡
        switch (loadBalanceType) {
            case LOADBALANCETYPE_POLLING:
                loadBalance = new PollingLoadBalance();
                break;
            case LOADBALANCETYPE_RANDOM:
                loadBalance = new RandomLoadBalance();
                break;
            case LOADBALANCETYPE_CONSISTENT:
                loadBalance = new ConsistentLoadBalance();
                break;
            default:
                throw new RuntimeException("loadBalanceType does not exist,loadBalanceType:" + loadBalanceType);
        }
        log.info("init loadBalance successfully");

        // 3.初始化 channelMap
        channelMap = new ConcurrentHashMap<>();
        log.info("init channelMap successfully");

        // 4.初始化 workerNum
        String workerNumProperty = LocalConfig.getProperty("rpc.client.netty.workerNum");
        workerNum = workerNumProperty != null ? Integer.valueOf(workerNumProperty) : DEFAULT_CLIENT_NETTY_WORKER_NUM;
        log.info("init client workerNum successfully");

        // 5.初始化 bootstrap
        RpcCodec rpcCodec = new RpcCodec();
        RpcResponseHandler rpcResponseHandler = new RpcResponseHandler();
        bootstrap = new Bootstrap()
                .group(new NioEventLoopGroup(workerNum))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 8, 4, 0, 0));
                        ch.pipeline().addLast(rpcCodec);
                        ch.pipeline().addLast(rpcResponseHandler);
                    }
                });
    }
}
