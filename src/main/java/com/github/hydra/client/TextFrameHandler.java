package com.github.hydra.client;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.hydra.constant.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TextFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    private ChannelManager.ChannelBox box;

    private boolean checkDelay;


    public TextFrameHandler(boolean checkDelay) {

        super();
        this.checkDelay = checkDelay;
    }


    @Override protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {

        if (log.isDebugEnabled()) {
            log.debug("content : " + msg.text());
        }

        if (this.box == null) {
            this.box = ChannelManager.getChannelBox(ctx.channel());
            if (this.box == null) {
                return;
            }
        }

        this.box.lastTimestamp = Util.nowMS();
        if (!this.checkDelay) {
            return;
        }

        JSONObject result = JSON.parseObject(msg.text());
        Long timestamp = result.getLong("ts");
        if (timestamp != null) {
            this.box.delay = this.box.lastTimestamp - timestamp;
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
