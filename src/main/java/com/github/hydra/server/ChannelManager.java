package com.github.hydra.server;


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

import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class ChannelManager {


    private static final Map<String, SubscribeBox> subscribeMap = Maps.newConcurrentMap();// channelId - subscribeBox

    private static final Map<String, ChannelGroup> nameSpace = Maps.newConcurrentMap();// topic - channelgroup

    private static final NavigableMap<String, String> userChannel = new ConcurrentSkipListMap<>();

    private static final ChannelGroup globalGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private static final AtomicInteger sessionCount = new AtomicInteger(0);

    private static final ChannelFutureListener listener = future -> removeChannel(future.channel());


    public static void addChannel(final Channel channel) {

        if (globalGroup.add(channel)) {
            channel.closeFuture().addListener(listener);
            sessionCount.incrementAndGet();
        }
    }


    public static void removeChannel(final Channel channel) {

        unSubscribe(channel);
        sessionCount.decrementAndGet();
    }


    public static boolean subscribe(final Channel channel, Set<String> names, String uid) {

        String id = channel.id().asLongText();
        SubscribeBox subscribeBox = subscribeMap.get(id);
        if (subscribeBox == null) {
            subscribeBox = new SubscribeBox();
            subscribeBox.channel = channel;
            subscribeBox.topics = Sets.newConcurrentHashSet(names);
            subscribeBox.uid = uid;


            SubscribeBox subscribeBoxOld = subscribeMap.putIfAbsent(id, subscribeBox);
            if (subscribeBoxOld != null) {
                subscribeBox = subscribeBoxOld;
                checkEqual(uid, subscribeBox.uid);
                subscribeBox.topics.addAll(names);
            }
        } else {
            checkEqual(uid, subscribeBox.uid);
            subscribeBox.topics.addAll(names);
        }

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
        return true;
    }


    public static boolean unSubscribe(final Channel channel) {

        String id = channel.id().asLongText();
        SubscribeBox subscribeBox = subscribeMap.remove(id);
        if (subscribeBox == null || subscribeBox.uid == null) {
            return true;
        }
        userChannel.remove(buildUidAndChannelId(subscribeBox.uid, id));
        return true;
    }


    public static void broadCastInNameSpace(String name, TextWebSocketFrame tws) {

        ChannelGroup channelGroup = nameSpace.get(name);
        if (channelGroup != null) {
            channelGroup.writeAndFlush(tws, ChannelMatchers.all(), true);
        }
    }


    public static void broadCast4User(String uid, final String txt) {

        NavigableMap<String, String> tmp = userChannel.subMap(buildUidAndChannelId(uid, null), true, buildUidAndChannelId(uid, "~"), true);
        tmp.forEach((k, v) -> {

            SubscribeBox subscribeBox = subscribeMap.get(v);
            if (subscribeBox != null) {
                subscribeBox.channel.writeAndFlush(new TextWebSocketFrame(txt));
            }
        });
    }


    public static void broadCastInAllOfWorld(TextWebSocketFrame tws) {

        globalGroup.writeAndFlush(tws);
    }


    private static String buildUidAndChannelId(String uid, String channelId) {

        return channelId == null ? uid + "|" : uid + "|" + channelId;
    }


    private static void checkEqual(Object a, Object b) {

        if (a == null) {
            Validate.isTrue(a == b);
        } else {
            Validate.isTrue(a.equals(b));
        }
    }


    public static void printLog() {

        log.info("session count : {} , subscribe count : {} , namespace count : {} , user session count : {} ",
                 sessionCount.get(), subscribeMap.size(), nameSpace.size(), userChannel.size());
    }


    static class SubscribeBox {


        public Channel channel;

        public Set<String> topics;

        public String uid;
    }
}
