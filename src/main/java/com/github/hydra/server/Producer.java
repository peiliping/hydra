package com.github.hydra.server;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.hydra.constant.Util;
import com.github.hydra.server.data.BizType;
import com.github.hydra.server.data.MsgType;
import com.github.hydra.server.data.PushMsg;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import java.util.Base64;
import java.util.Map;


@Slf4j
public class Producer {


    private Redisson redis;

    private RTopic topic;

    private Map<String, RateLimiter> limiters = Maps.newConcurrentMap();


    public Producer(String address, String pwd, int dbnum, String topicName) {

        Config redisConfig = new Config();
        SingleServerConfig singleServerConfig = redisConfig.useSingleServer();
        singleServerConfig.setAddress(address);
        singleServerConfig.setPassword(pwd);
        singleServerConfig.setDatabase(dbnum);
        singleServerConfig.setConnectionPoolSize(16);
        singleServerConfig.setConnectionMinimumIdleSize(1);
        singleServerConfig.setClientName("hydra-" + Util.nowSec());
        singleServerConfig.setDnsMonitoringInterval(10000);
        this.redis = (Redisson) Redisson.create(redisConfig);
        this.redis.getAtomicLong("hydra-health-check").incrementAndGet();
        this.topic = this.redis.getTopic(topicName, new ByteArrayCodec());
    }


    public void start() {

        this.topic.addListener(byte[].class, (channel, msg) -> {
            try {
                String message = new String(msg);

                if (log.isDebugEnabled()) {
                    log.debug("subscribe data : {} .", message);
                }

                if (StringUtils.isEmpty(message) || message.length() > 1024 * 10) {
                    return;
                }

                JSONObject jsonObject = JSON.parseObject(message);

                String biz = jsonObject.getString("biz");
                BizType bizType = BizType.of(biz);
                if (bizType == null) {
                    return;
                }

                String type = jsonObject.getString("type");
                MsgType msgType = MsgType.of(type);
                if (msgType == null) {
                    return;
                }

                String topic = jsonObject.getString("topic");
                if (topic == null) {
                    return;
                }

                String nameSpace = Util.buildNameSpace(biz, type, topic);
                if (permit(nameSpace, 1)) {
                    sendTextFrame(nameSpace, biz, type, topic, jsonObject.getJSONArray("data"), false);
                }
            } catch (Throwable e) {
                log.error("subscribe data error : ", e);
            }
        });
    }


    public static void sendTextFrame(String nameSpace, String biz, String type, String topic, Object data, boolean zip) {

        data = zip ? Base64.getEncoder().encodeToString(Util.compressGzip((String) data)) : data;
        PushMsg pushMsg = PushMsg.builder().biz(biz).type(type).topic(topic).data(data).zip(zip).build();
        TextWebSocketFrame frame = new TextWebSocketFrame(JSON.toJSONString(pushMsg));
        ChannelManager.broadCastInNameSpace(nameSpace, frame);
    }


    private boolean permit(String nameSpace, double ratio) {

        RateLimiter rateLimiter = this.limiters.get(nameSpace);
        if (rateLimiter == null) {
            rateLimiter = RateLimiter.create(ratio);
            this.limiters.put(nameSpace, rateLimiter);
        }
        return rateLimiter.tryAcquire();
    }
}
