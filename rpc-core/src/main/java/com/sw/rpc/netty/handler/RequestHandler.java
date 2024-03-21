package com.sw.rpc.netty.handler;

import com.sw.rpc.core.server.RpcServer;
import com.sw.rpc.dto.detail.Ping;
import com.sw.rpc.dto.detail.Pong;
import com.sw.rpc.dto.detail.RpcRequest;
import com.sw.rpc.dto.detail.RpcResponse;
import com.sw.rpc.utils.ReflectionUtil;
import com.sw.rpc.utils.ServiceRegistryUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@ChannelHandler.Sharable
@Slf4j
public class RequestHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RpcRequest) {
            dealWithRpcRequest(ctx, (RpcRequest) msg);
        } else if (msg instanceof Ping) {
            dealWithPing(ctx, (Ping) msg);
        }
        ctx.fireChannelRead(msg);
    }


    protected void dealWithRpcRequest(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        // 根据服务名(接口名+”#“+实现名)获取 server
        String serviceName = ServiceRegistryUtil.constructServiceName(rpcRequest.getInterfaceName(), rpcRequest.getImplName());
        Object serverBean = RpcServer.registeredServiceMap.getOrDefault(serviceName, null);

        // 反射获取方法
        Class<?> iClass = ReflectionUtil.getClassForName(rpcRequest.getInterfaceName());
        Method method = iClass.getMethod(rpcRequest.getMethodName(), ReflectionUtil.getClassesForNames(rpcRequest.getParamTypes()));

        // 执行方法得到结果
        RpcResponse rpcResponse;
        try {
            Object result = method.invoke(serverBean, rpcRequest.getParams());
            rpcResponse = new RpcResponse(rpcRequest.getRequestId(), result, null);
        } catch (Exception e) {
            rpcResponse = new RpcResponse(rpcRequest.getRequestId(), null, e.getCause().getMessage());
            e.printStackTrace();
        }
        // 把结果返回给 client
        ctx.channel().writeAndFlush(rpcResponse);
    }

    protected void dealWithPing(ChannelHandlerContext ctx, Ping ping) throws Exception {
        // 把 pong 返回给 client
        log.debug("收到 ping, 来自客户端:{}", ctx.channel().remoteAddress());
        // 返回 pong
        ctx.channel().writeAndFlush(new Pong());
    }
}
