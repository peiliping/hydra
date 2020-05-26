package com.github.hydra.client;


import com.github.hydra.constant.Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BinaryFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {


    private ChannelManager.ChannelBox box;

    private boolean unCompressGzip;


    public BinaryFrameHandler(boolean unCompressGzip) {

        this.unCompressGzip = unCompressGzip;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) {

        if (this.box == null) {
            this.box = ChannelManager.getChannelBox(ctx.channel());
            if (this.box == null) {
                return;
            }
        }
        this.box.lastTimestamp = Util.nowMS();

        if (this.unCompressGzip) {
            ByteBuf byteBuf = Unpooled.copiedBuffer(msg.content());
            String content = Util.unCompressGzip(byteBuf.array());
            if (log.isDebugEnabled()) {
                log.debug("data length : {} , content : {} ", content.length(), content);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("data length : {} ", msg.content().capacity());
            }
        }
    }
}
