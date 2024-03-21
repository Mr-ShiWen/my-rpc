package com.sw.rpc.netty.handler;

import com.sw.rpc.dto.detail.Ping;
import com.sw.rpc.dto.detail.Pong;
import com.sw.rpc.dto.detail.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import static com.sw.rpc.core.client.RpcClient.respMap;

@ChannelHandler.Sharable
@Slf4j
public class ResponseHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RpcResponse) {
            dealWithRpcResponse(ctx, (RpcResponse) msg);
        } else if (msg instanceof Pong) {
            dealWithPong(ctx, (Pong) msg);
        }
        ctx.fireChannelRead(msg);
    }


    protected void dealWithRpcResponse(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        // 把结果放到对应的 promise
        Promise<RpcResponse> promise = respMap.getOrDefault(rpcResponse.getRequestId(), null);
        if (promise != null) {
            promise.setSuccess(rpcResponse);
        }
    }

    protected void dealWithPong(ChannelHandlerContext ctx, Pong pong) throws Exception {
        log.debug("收到服务器返回的 pong");

    }
}
