package com.github.hydra.server;


import com.alibaba.fastjson.JSON;
import com.github.hydra.constant.Util;
import com.github.hydra.server.data.Answer;
import com.github.hydra.server.data.BizType;
import com.github.hydra.server.data.Command;
import com.github.hydra.server.data.MsgType;
import com.google.common.collect.Sets;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
public class TextFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    @Override protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

        try {
            String message = msg.text();
            if (log.isDebugEnabled()) {
                log.debug("content : " + message);
            }

            Command cmd = JSON.parseObject(message, Command.class);

            if (Command.PING.equals(cmd.getEvent())) {
                ChannelManager.heartBeat(ctx.channel());
                sendAnswer(ctx, Answer.builder().event(Command.PONG).build());
                return;
            }

            Validate.notNull(BizType.of(cmd.getBiz()));
            Validate.notNull(MsgType.of(cmd.getType()));

            if (cmd.getTopics() == null) {
                cmd.setTopics(Sets.newHashSet());
            }
            Set<String> topics = cmd.getTopics().stream().map(s -> Util.buildNameSpace(cmd.getBiz(), cmd.getType(), s)).collect(Collectors.toSet());

            if (Command.SUBSCRIBE.equals(cmd.getEvent())) {
                sendAnswer(ctx, Answer.builder().event(cmd.getEvent()).biz(cmd.getBiz()).type(cmd.getType()).topics(cmd.getTopics()).build());
                ChannelManager.subscribe(ctx.channel(), topics, cmd.getToken());
            } else if (Command.UNSUBSCRIBE.equals(cmd.getEvent())) {
                sendAnswer(ctx, Answer.builder().event(cmd.getEvent()).biz(cmd.getBiz()).type(cmd.getType()).topics(cmd.getTopics()).build());
                ChannelManager.unSubscribeTopics(ctx.channel(), topics);
            } else {
                return;
            }
        } catch (Throwable e) {
            log.error("textFrameHandler error : ", e);
        }
    }


    private void sendAnswer(ChannelHandlerContext ctx, Answer answer) {

        ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(answer)));
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
}
