package com.github.hydra.client;


import com.github.hydra.constant.Util;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class ChannelManager {


    private static final List<ChannelFuture> channelFutures = Lists.newArrayList();

    private static final Map<String, ChannelBox> inProcessing = Maps.newConcurrentMap();

    private static final ChannelFutureListener listener = future -> removeChannel(future.channel());

    private static final AtomicInteger subscribeCount = new AtomicInteger(0);


    public synchronized static void addChannelFuture(ChannelFuture channelFuture) {

        channelFutures.add(channelFuture);
    }


    public static void addChannel(final Channel channel) {

        ChannelBox box = new ChannelBox(channel);
        if (inProcessing.putIfAbsent(channel.id().asLongText(), box) == null) {
            channel.closeFuture().addListener(listener);
        }
    }


    public static void removeChannel(final Channel channel) {

        inProcessing.remove(channel.id().asLongText());
    }


    public static ChannelBox getChannelBox(final Channel channel) {

        return inProcessing.get(channel.id().asLongText());
    }


    public static void subscribe(String subscribeString, long subscribeInterval) {

        if (subscribeCount.get() == channelFutures.size()) {
            return;
        }

        String[] subscribeItems = null;
        for (int i = 0; i < channelFutures.size(); i++) {
            ChannelBox box = getChannelBox(channelFutures.get(i).channel());
            if (box == null || box.subscribed || !box.channel.isActive()) {
                continue;
            }
            if (subscribeItems == null) {
                subscribeItems = subscribeString.split("\\$");
            }
            for (String item : subscribeItems) {
                if (StringUtils.isNotBlank(item)) {
                    TextWebSocketFrame frame = new TextWebSocketFrame(item);
                    box.channel.writeAndFlush(frame);
                    Util.sleepMS(subscribeInterval);
                }
            }
            box.subscribed = true;
            subscribeCount.incrementAndGet();
        }
    }


    public static void heartBeat(String heartBeatString) {

        long now = Util.nowMS();
        String hbStr = String.format(heartBeatString, now);
        for (int i = 0; i < channelFutures.size(); i++) {
            ChannelBox box = getChannelBox(channelFutures.get(i).channel());
            if (box == null || !box.channel.isActive()) {
                continue;
            }
            box.channel.writeAndFlush(new TextWebSocketFrame(hbStr));
        }
    }


    public static void monitor() {

        int subscribedCount = 0, realTimeCount = 0;
        long now = Util.nowMS();
        for (int i = 0; i < channelFutures.size(); i++) {
            ChannelBox box = getChannelBox(channelFutures.get(i).channel());
            if (box == null || !box.channel.isActive()) {
                continue;
            }
            if (box.subscribed) {
                subscribedCount++;
            }
            if ((now - box.lastTimestamp) < Util.MIN_1) {
                realTimeCount++;
            }
        }
        log.info("monitor : channel : {}, subscribed : {}, realtime : {} .", inProcessing.size(), subscribedCount, realTimeCount);
    }


    static class ChannelBox {


        public Channel channel;

        public boolean subscribed = false;

        public long lastTimestamp = Util.nowMS();


        public ChannelBox(Channel c) {

            this.channel = c;
        }
    }
}
