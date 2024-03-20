package com.sw.rpc.netty.handler;

import com.sw.rpc.core.server.RpcServer;
import com.sw.rpc.dto.detail.RpcRequest;
import com.sw.rpc.dto.detail.RpcResponse;
import com.sw.rpc.utils.ReflectionUtil;
import com.sw.rpc.utils.ServiceRegistryUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.sw.rpc.error.impl.ErrorInfoEnum.RpcError;

@ChannelHandler.Sharable
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        // 根据服务名(接口名+”#“+实现名)获取 server
        String serviceName = ServiceRegistryUtil.constructServiceName(rpcRequest.getInterfaceName(), rpcRequest.getImplName());
        Object serverBean = RpcServer.registeredServiceMap.getOrDefault(serviceName, null);

        // 反射获取方法，执行得到结果
        Class<?> iClass = ReflectionUtil.getClassForName(rpcRequest.getInterfaceName());
        Method method = iClass.getMethod(rpcRequest.getMethodName(), ReflectionUtil.getClassesForNames(rpcRequest.getParamTypes()));

        RpcResponse rpcResponse;
        try {
            Object result = method.invoke(serverBean, rpcRequest.getParams());
            rpcResponse = new RpcResponse(rpcRequest.getRequestId(), result, null);
        } catch (Exception e) {
            rpcResponse = new RpcResponse(rpcRequest.getRequestId(), null, e.getCause().getMessage());
            e.printStackTrace();
        }

        ctx.channel().writeAndFlush(rpcResponse);
    }
}
