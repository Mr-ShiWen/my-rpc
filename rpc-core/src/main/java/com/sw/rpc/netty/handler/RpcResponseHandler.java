package com.sw.rpc.netty.handler;

import com.sw.rpc.dto.detail.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;

import static com.sw.rpc.core.client.RpcClient.respMap;

@ChannelHandler.Sharable
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        Promise<RpcResponse> promise = respMap.getOrDefault(rpcResponse.getRequestId(), null);
        if (promise != null) {
            promise.setSuccess(rpcResponse);
        }
    }
}
