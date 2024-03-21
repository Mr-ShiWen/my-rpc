package com.sw.rpc.netty.handler;

import com.sw.rpc.dto.detail.Ping;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientIdleHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent) evt;
        switch (event.state()) {
            case WRITER_IDLE:
                log.debug("write idle reach,send ping");
                ctx.channel().writeAndFlush(new Ping());
                break;
            case READER_IDLE:
                log.debug("read idle reach,close channel");
                ctx.channel().close();
        }
        ctx.fireUserEventTriggered(evt);
    }
}
