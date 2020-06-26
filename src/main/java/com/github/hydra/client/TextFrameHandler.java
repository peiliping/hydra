package com.github.hydra.client;


import com.alibaba.fastjson.JSON;
import com.github.hydra.client.data.PushMsg;
import com.github.hydra.constant.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;


@Slf4j
public class TextFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    private ChannelManager.ChannelBox box;

    private boolean unCompressGzip;


    public TextFrameHandler(boolean unCompressGzip) {

        this.unCompressGzip = unCompressGzip;
    }


    @Override protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {

        if (this.box == null) {
            this.box = ChannelManager.getChannelBox(ctx.channel());
            if (this.box == null) {
                return;
            }
        }
        this.box.lastTimestamp = Util.nowMS();

        String text = msg.text();
        if (log.isDebugEnabled()) {
            log.debug("data length : {} , content : {} ", text.length(), text);
        }

        if (this.unCompressGzip) {
            PushMsg pushMsg = JSON.parseObject(text, PushMsg.class);
            if (pushMsg.getData() != null && pushMsg.isZip()) {
                String data = pushMsg.getData().toString();
                String dataStr = Util.unCompressGzip(Base64.getDecoder().decode(data));
                if (log.isDebugEnabled()) {
                    log.debug("json compress data : {} ", dataStr);
                }
            }
        }
    }
}
