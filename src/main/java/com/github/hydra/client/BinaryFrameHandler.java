package com.github.hydra.client;


import com.github.hydra.constant.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BinaryFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {


    private ChannelManager.ChannelBox box;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws InterruptedException {

        if (log.isDebugEnabled()) {
            log.debug("binary data length : {} ", msg.content().capacity());
        }
        this.box = (this.box == null ? ChannelManager.getChannelBox(ctx.channel()) : this.box);
        if (this.box != null) {
            box.lastTimestamp = Util.nowMS();
        }
    }
}
