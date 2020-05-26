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


@Slf4j
public class ChannelManager {


    private static final List<ChannelFuture> channelFutures = Lists.newArrayList();

    private static final Map<String, ChannelBox> inProcessing = Maps.newConcurrentMap();

    private static final ChannelFutureListener listener = future -> removeChannel(future.channel());


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


    public static void subscribe(String subscribeString, long subInterval) {

        String[] subscribeItems = subscribeString.split("\\$");
        for (int i = 0; i < channelFutures.size(); i++) {
            ChannelBox box = getChannelBox(channelFutures.get(i).channel());
            if (box == null || !box.channel.isActive() || !box.channel.isWritable()) {
                continue;
            }
            if (box.subscribed) {
                continue;
            }
            for (String item : subscribeItems) {
                if (StringUtils.isNotBlank(item)) {
                    TextWebSocketFrame frame = new TextWebSocketFrame(item);
                    Util.sleepMS(subInterval);
                    box.channel.writeAndFlush(frame);
                }
            }
            box.subscribed = true;
        }

    }


    public static void scan(boolean heartBeat, String heartBeatString) {

        int subscribedCount = 0;
        int realTimeCount = 0;

        long now = Util.nowMS();
        String hbStr = heartBeat ? String.format(heartBeatString, now) : null;

        for (int i = 0; i < channelFutures.size(); i++) {
            ChannelBox box = getChannelBox(channelFutures.get(i).channel());
            if (box == null || !box.channel.isActive() || !box.channel.isWritable()) {
                continue;
            }
            if (heartBeat) {
                box.channel.writeAndFlush(new TextWebSocketFrame(hbStr));
            }
            if (box.subscribed) {
                subscribedCount++;
            }
            if (Util.MIN_1 > (now - box.lastTimestamp)) {
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
