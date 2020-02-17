package com.github.hydra.client;


import com.alibaba.fastjson.JSON;
import com.github.hydra.constant.Result;
import com.github.hydra.constant.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TextFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    private ChannelManager.ChannelBox box;

    private boolean parseResult;


    public TextFrameHandler(boolean parseResult) {

        super();
        this.parseResult = parseResult;
    }


    @Override protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("content : " + msg.text());
        }

        this.box = (this.box == null ? ChannelManager.getChannelBox(ctx.channel()) : this.box);
        if (this.box != null) {
            box.lastTimestamp = Util.nowMS();
            if (this.parseResult) {
                Result result = JSON.parseObject(msg.text(), Result.class);
                box.delay = box.lastTimestamp - result.timestamp;
            }
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
