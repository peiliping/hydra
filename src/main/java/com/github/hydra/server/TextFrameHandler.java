package com.github.hydra.server;


import com.alibaba.fastjson.JSON;
import com.github.hydra.constant.Command;
import com.github.hydra.constant.Result;
import com.github.hydra.constant.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TextFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    @Override protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

        try {
            String message = msg.text();
            if (log.isDebugEnabled()) {
                log.debug("content : " + message);
            }
            Result result = new Result();
            result.timestamp = Util.nowMS();
            Command command = JSON.parseObject(message, Command.class);
            if (Command.SUBSCRIBE.equals(command.type)) {
                result.success = ChannelManager.subscribe(ctx.channel(), command.topics, command.uid);
            } else if (Command.UNSUBSCRIBE.equals(command.type)) {
                result.success = ChannelManager.unSubscribe(ctx.channel());
            }
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));
        } catch (Throwable e) {
            log.error("error : ", e);
        }
    }


    @Override public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            if (log.isDebugEnabled()) {
                log.debug("hand shake complete : " + ctx.channel());
            }
            ChannelManager.addChannel(ctx.channel());
        }
        super.userEventTriggered(ctx, evt);
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
