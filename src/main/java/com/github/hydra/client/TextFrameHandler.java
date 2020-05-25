package com.github.hydra.client;


import com.github.hydra.constant.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TextFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    private ChannelManager.ChannelBox box;


    @Override protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {

        if (this.box == null) {
            this.box = ChannelManager.getChannelBox(ctx.channel());
            if (this.box == null) {
                return;
            }
        }
        this.box.lastTimestamp = Util.nowMS();

        if (log.isDebugEnabled()) {
            log.debug("text data length : {} ", msg.content().capacity());
            log.debug("content : " + msg.text());
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("channel active : " + ctx.channel());
        }
        super.channelActive(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("channel inactive ：" + ctx.channel());
        }
        super.channelInactive(ctx);
    }
}
