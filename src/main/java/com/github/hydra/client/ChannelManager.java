package com.github.hydra.client;


import com.github.hydra.constant.Util;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;


@Slf4j
public class ChannelManager {


    private static final List<ChannelFuture> channelFutures = Lists.newArrayList();

    private static final Map<String, ChannelBox> inProcessing = Maps.newConcurrentMap();

    private static final ChannelFutureListener listener = future -> removeChannel(future.channel());


    public synchronized static void addChannelFuture(ChannelFuture channelFuture) {

        channelFutures.add(channelFuture);
    }


    public static void addChannel(final Channel channel) {

        ChannelBox box = new ChannelBox();
        box.channel = channel;
        if (inProcessing.putIfAbsent(channel.id().asLongText(), box) == null) {
            channel.closeFuture().addListener(listener);
        }
    }


    public static void removeChannel(final Channel channel) {

        if (inProcessing.remove(channel.id().asLongText()) != null) {

        }
    }


    public static ChannelBox getChannelBox(final Channel channel) {

        return inProcessing.get(channel.id().asLongText());
    }


    public static void scan(long realTimeLevel, boolean subscribe, String subscribeString, boolean heartBeat, String heartBeatString) {

        int connectionsCount = channelFutures.size();
        int channelsCount = inProcessing.size();
        int subscribedCount = 0;
        int realTimeCount = 0;
        for (int i = 0; i < connectionsCount; i++) {
            ChannelBox box = getChannelBox(channelFutures.get(i).channel());
            if (box == null) {
                continue;
            }
            if (!box.channel.isActive() || !box.channel.isWritable()) {
                continue;
            }

            if (box.subscribed) {
                subscribedCount++;
                if (box.delay < realTimeLevel && Util.nowMS() - box.lastTimestamp < 3000) {
                    realTimeCount++;
                }
                if (heartBeat) {
                    box.channel.writeAndFlush(new TextWebSocketFrame(String.format(heartBeatString, Util.nowMS())));
                }
            } else if (subscribe) {
                box.subscribed = true;
                TextWebSocketFrame frame = new TextWebSocketFrame(subscribeString);
                box.channel.writeAndFlush(frame);
            }
        }
        log.info("connections count : {}, channel count : {}, subscribed count : {}, realtime count : {}", connectionsCount, channelsCount,
                 subscribedCount, realTimeCount);
    }


    static class ChannelBox {


        public Channel channel;

        public boolean subscribed = false;

        public long delay = 0L;

        public long lastTimestamp = Util.nowMS();

    }
}
