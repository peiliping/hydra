package com.github.hydra.server;


import com.github.hydra.constant.Util;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatchers;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class ChannelManager {


    private static final Map<String, SubscribeBox> subscribeMap = Maps.newConcurrentMap();// channelId - subscribeBox

    private static final Map<String, ChannelGroup> nameSpace = Maps.newConcurrentMap();// topic - channelgroup

    private static final NavigableMap<String, String> userChannel = new ConcurrentSkipListMap<>();

    private static final ChannelGroup globalGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private static final AtomicInteger sessionCount = new AtomicInteger(0);

    private static final AtomicLong broadcastCount = new AtomicLong(0L);

    private static final ChannelFutureListener listener = future -> removeChannel(future.channel());


    public static void addChannel(final Channel channel) {

        if (globalGroup.add(channel)) {
            subscribeMap.putIfAbsent(channel.id().asLongText(), new SubscribeBox(channel));
            sessionCount.incrementAndGet();
            channel.closeFuture().addListener(listener);
        }
    }


    public static void removeChannel(final Channel channel) {

        unSubscribeAll(channel);
        sessionCount.decrementAndGet();
    }


    public static void heartBeat(final Channel channel) {

        String id = channel.id().asLongText();
        SubscribeBox subscribeBox = subscribeMap.get(id);
        if (subscribeBox == null) {
            return;
        }
        if (!subscribeBox.open) {
            return;
        }
        subscribeBox.lastHeartBeatTime = Util.nowMS();
    }


    public static void subscribe(final Channel channel, Set<String> names, String uid) {

        String id = channel.id().asLongText();
        SubscribeBox subscribeBox = subscribeMap.get(id);
        if (subscribeBox == null) {
            log.error("it is not normal .");
            subscribeBox = new SubscribeBox(channel);
            SubscribeBox old = subscribeMap.putIfAbsent(id, subscribeBox);
            if (old != null) {
                log.error("it is not normal concurrent .");
                subscribeBox = old;
            }
        }
        if (!subscribeBox.open) {
            return;
        }
        Validate.isTrue(subscribeBox.addChannel(channel));
        Validate.isTrue(subscribeBox.addUid(uid));
        subscribeBox.topics.addAll(names);
        names.forEach(s -> {
            ChannelGroup channelGroup = nameSpace.get(s);
            if (channelGroup == null) {
                channelGroup = new DefaultChannelGroup(new DefaultEventExecutor());
                ChannelGroup tmpChannelGroup = nameSpace.putIfAbsent(s, channelGroup);
                if (tmpChannelGroup != null) {
                    channelGroup = tmpChannelGroup;
                }
            }
            channelGroup.add(channel);
        });
        if (uid != null) {
            userChannel.put(buildUidAndChannelId(uid, id), id);
        }
    }


    public static void unSubscribeAll(final Channel channel) {

        String id = channel.id().asLongText();
        SubscribeBox subscribeBox = subscribeMap.remove(id);
        if (subscribeBox == null) {
            return;
        }
        subscribeBox.topics.forEach(s -> {

            ChannelGroup channelGroup = nameSpace.get(s);
            if (channelGroup != null) {
                channelGroup.remove(channel);
            }
        });
        if (subscribeBox.uid != null) {
            userChannel.remove(buildUidAndChannelId(subscribeBox.uid, id));
        }
    }


    public static void unSubscribeTopics(final Channel channel, Set<String> names) {

        String id = channel.id().asLongText();
        SubscribeBox subscribeBox = subscribeMap.get(id);
        if (subscribeBox == null) {
            return;
        }
        if (!subscribeBox.open) {
            return;
        }
        subscribeBox.topics.removeAll(names);
        names.forEach(s -> {

            ChannelGroup channelGroup = nameSpace.get(s);
            if (channelGroup != null) {
                channelGroup.remove(channel);
            }
        });
    }


    public static void broadCastInNameSpace(String name, String txt) {

        ChannelGroup channelGroup = nameSpace.get(name);
        if (channelGroup != null) {
            TextWebSocketFrame frame = new TextWebSocketFrame(txt);
            channelGroup.writeAndFlush(frame, ChannelMatchers.all(), true);
            broadcastCount.incrementAndGet();
        }
    }


    public static void broadCast4User(String uid, final String txt) {

        TextWebSocketFrame frame = new TextWebSocketFrame(txt);
        NavigableMap<String, String> tmp = userChannel.subMap(buildUidAndChannelId(uid, null), true, buildUidAndChannelId(uid, "~"), true);
        tmp.forEach((k, v) -> {

            SubscribeBox subscribeBox = subscribeMap.get(v);
            if (subscribeBox != null) {
                subscribeBox.channel.writeAndFlush(frame);
            }
        });
    }


    public static void broadCastInAllOfWorld(String txt) {

        TextWebSocketFrame frame = new TextWebSocketFrame(txt);
        globalGroup.writeAndFlush(frame);
    }


    private static String buildUidAndChannelId(String uid, String channelId) {

        return channelId == null ? uid + "|" : uid + "|" + channelId;
    }


    public static void monitorLog(boolean checkIdle) {

        int expiredSession = 0;
        if (checkIdle) {
            long now = Util.nowMS();
            Iterator<Map.Entry<String, SubscribeBox>> it = subscribeMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, SubscribeBox> entry = it.next();
                SubscribeBox box = entry.getValue();
                if (box.open) {
                    if ((now - box.lastHeartBeatTime) >= Util.SEC_45) {
                        box.open = false;
                    }
                } else {
                    if ((now - box.lastHeartBeatTime) >= Util.MIN_1) {
                        expiredSession++;
                        if (entry.getValue().channel != null) {
                            entry.getValue().channel.close();
                        }
                    }
                }
            }
        }
        log.info("session : {} , subscribe : {} , namespace : {} , user session : {} , expired session : {} , broadcast : {}",
                 sessionCount.get(), subscribeMap.size(), nameSpace.size(), userChannel.size(), expiredSession, broadcastCount.get());
    }


    static class SubscribeBox {


        public boolean open;

        public Channel channel;

        public Set<String> topics;

        public String uid;

        public long lastHeartBeatTime;


        public SubscribeBox(Channel c) {

            this.open = true;
            this.channel = c;
            this.topics = Sets.newHashSet();
            this.lastHeartBeatTime = Util.nowMS();
        }


        public boolean addChannel(Channel c) {

            if (this.channel == null) {
                this.channel = c;
                return true;
            }
            return this.channel == c;
        }


        public boolean addUid(String uid) {

            if (this.uid == null) {
                this.uid = uid;
                return true;
            }
            return this.uid.equals(uid);
        }
    }
}
