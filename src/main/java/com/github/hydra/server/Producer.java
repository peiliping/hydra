package com.github.hydra.server;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.hydra.constant.Util;
import com.github.hydra.server.data.BizType;
import com.github.hydra.server.data.MsgType;
import com.github.hydra.server.data.PushMsg;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
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
                if (StringUtils.isEmpty(message)) {
                    return;
                }
                if (log.isDebugEnabled()) {
                    log.debug("subscribe data : {} .", message);
                }

                JSONObject jsonObject = JSON.parseObject(message);

                BizType bizType = BizType.of(jsonObject.getString("bizType"));
                if (bizType == null) {
                    return;
                }

                MsgType msgType = MsgType.of(jsonObject.getString("msgType"));
                if (msgType == null) {
                    return;
                }

                String topic = jsonObject.getString("topic");
                if (topic == null) {
                    return;
                }

                String data = jsonObject.getString("data");
                if (StringUtils.isEmpty(data)) {
                    return;
                }
                boolean zip = false;
                if (data.length() >= 5 * 1024) {
                    data = Base64.getEncoder().encodeToString(Util.compressGzip(data));
                    zip = true;
                }
                PushMsg pushMsg = PushMsg.builder().biz(bizType).type(msgType).topic(topic).data(data).zip(zip).build();
                String dataStr = JSON.toJSONString(pushMsg);

                switch (bizType) {
                    case NAMESPCACE:
                        String nameSpace = Util.buildNameSpace(bizType.name, msgType.name, topic);
                        if (permit(nameSpace, 1)) {
                            ChannelManager.broadCastInNameSpace(nameSpace, dataStr);
                        }
                        break;
                    case USER:
                        String uid = jsonObject.getString("uid");
                        if (StringUtils.isEmpty(uid)) {
                            return;
                        }
                        ChannelManager.broadCast4User(uid, dataStr);
                        break;
                    case BROADCAST:
                        ChannelManager.broadCastInAllOfWorld(dataStr);
                        break;
                }
            } catch (Throwable e) {
                log.error("subscribe data error : ", e);
            }
        });
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
