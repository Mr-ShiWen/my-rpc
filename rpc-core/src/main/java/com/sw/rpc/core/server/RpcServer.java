package com.sw.rpc.core.server;

import com.sw.rpc.config.LocalConfig;
import com.sw.rpc.assist.registry.ServiceRegister;
import com.sw.rpc.assist.registry.impl.ServiceRegisterImpl;
import com.sw.rpc.netty.handler.RpcCodec;
import com.sw.rpc.netty.handler.RequestHandler;
import com.sw.rpc.netty.handler.ServerIdleHandler;
import com.sw.rpc.utils.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import static com.sw.rpc.constant.Constant.*;

@Slf4j
public class RpcServer {
    public static final ConcurrentHashMap<String, Object> registeredServiceMap = new ConcurrentHashMap<>();
    private ServiceRegister serviceRegister;
    private String serverIp;
    private int serverPort;
    private int workerNum;
    private boolean started;

    public boolean addServiceWithClass(Class<?> serviceImplClass) {
        Object serviceImplBean;
        try {
            serviceImplBean = serviceImplClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return addServiceWithClazzAndBean(serviceImplClass, serviceImplBean);
    }

    public boolean addServiceWithBean(Object serviceImplBean) {
        return addServiceWithClazzAndBean(serviceImplBean.getClass(), serviceImplBean);
    }

    private boolean addServiceWithClazzAndBean(Class<?> serviceImplClass, Object serviceImplBean) {
        // 1.检查是否实现接口
        if (!hasInterface(serviceImplClass)) {
            log.error("service don't implement any interface, service:{}", serviceImplClass.getName());
            return false;
        }

        // 2.获取服务名
        String interfaceName = InterfaceUtil.getInterfaceName(serviceImplClass);
        String implName = AnnotationInfoUtil.getImplementNameForRpcServerInfo(serviceImplClass);
        String serviceName = ServiceRegistryUtil.constructServiceName(interfaceName, implName);
        if (registeredServiceMap.contains(serviceName)) {
            //已经注册过了
            return true;
        }

        // 3.注册服务到 zookeeper
        if (serviceRegister != null) {
            try {
                serviceRegister.registerService(serviceName, new InetSocketAddress(serverIp, serverPort));
            } catch (Exception e) {
                log.error("fail to register service to zookeeper");
                throw new RuntimeException(e);
            }
        }

        // 4.添加服务到 map ，方便捞取执行
        try {
            registeredServiceMap.put(serviceName, serviceImplBean);
        } catch (Exception e) {
            log.error("fail to add service instance to map");
            throw new RuntimeException(e);
        }
        return true;
    }

    private boolean hasInterface(Class<?> serviceImplClass) {
        return !CollectionUtil.isEmpty(serviceImplClass.getInterfaces());
    }

    public void start() {
        if (!started) {
            synchronized (this) {
                if (!started) {
                    startRcpServer();
                    started = true;
                }
            }
        }
    }

    private void startRcpServer() {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup(workerNum);

        RpcCodec rpcCodec = new RpcCodec();
        RequestHandler requestHandler = new RequestHandler();
        try {
            ChannelFuture future = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(READ_IDLE_SECOND, 0, 0));
                            ch.pipeline().addLast(new ServerIdleHandler());
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 8, 4, 0, 0));
                            ch.pipeline().addLast(rpcCodec);
                            ch.pipeline().addLast(requestHandler);
                        }
                    }).bind(serverPort).sync();
            ChannelFuture closedFuture = future.channel().closeFuture();
            closedFuture.addListener(promise -> {
                boss.shutdownGracefully();
                worker.shutdownGracefully();
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public RpcServer() {
        initRpcServer();
    }

    private void initRpcServer() {
        // 初始化服务注册器
        try {
            // 读取 zookeeper 的集群 ip
            String connections = LocalConfig.getProperty("zookeeper.connections");

            // 读取 zookeeper 的连接超时配置
            String timeoutProperty = LocalConfig.getProperty("zookeeper.sessionTimeOut");
            int sessionTimeOut = timeoutProperty == null ? ZOOKEEPER_SESSION_TIME_OUT : Integer.valueOf(LocalConfig.getProperty("zookeeper.sessionTimeOut"));

            // 读取 zookeeper 的 rpcRootPath
            String rpcRootPath = LocalConfig.getProperty("zookeeper.rpcRootPath");
            rpcRootPath = rpcRootPath == null ? RPC_ROOT_PATH : rpcRootPath;

            // 创建注册中心，用于把服务注册进 zookeeper
            serviceRegister = new ServiceRegisterImpl(connections, sessionTimeOut, rpcRootPath);
            log.info("init serviceRegister successfully");
        } catch (Exception e) {
            log.error("fail to init serviceRegister");
            e.printStackTrace();
        }

        // 初始化 ip、port
        serverIp = AddressUtil.getLocalHost();
        String portProperty = LocalConfig.getProperty("rpc.server.port");
        serverPort = portProperty != null ? Integer.valueOf(portProperty) : DEFAULT_RPC_PORT;
        log.info("init ip & port successfully");

        // 初始化 netty worker num
        String workerNumProperty = LocalConfig.getProperty("rpc.server.netty.workerNum");
        workerNum = workerNumProperty != null ? Integer.valueOf(workerNumProperty) : DEFAULT_SERVER_NETTY_WORKER_NUM;
        log.info("init server workerNum successfully");
    }
}
