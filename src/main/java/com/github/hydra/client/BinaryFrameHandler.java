package com.github.hydra.client;


import com.github.hydra.constant.Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;


@Slf4j
public class BinaryFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {


    private ChannelManager.ChannelBox box;

    private boolean unCompressGzip;


    public BinaryFrameHandler(boolean unCompressGzip) {

        this.unCompressGzip = unCompressGzip;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws InterruptedException {

        if (log.isDebugEnabled()) {
            log.debug("binary data length : {} ", msg.content().capacity());
            ByteBuf byteBuf = Unpooled.copiedBuffer(msg.content());
            log.debug(unCompressGzip(byteBuf.array()));
        }
        this.box = (this.box == null ? ChannelManager.getChannelBox(ctx.channel()) : this.box);
        if (this.box != null) {
            box.lastTimestamp = Util.nowMS();
        }
    }


    private static String unCompressGzip(byte[] bytes) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            GZIPInputStream unGzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = unGzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toString();
        } catch (Exception e) {
            log.error("unCompressGzip error : ", e);
        }
        return "";
    }
}
