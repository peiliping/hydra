package com.github.hydra.client;


import com.github.hydra.constant.Util;
import com.github.hydra.constant.WebSocketSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.lang3.Validate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.hydra.client.CMD.*;


@Slf4j
public class Start {


    public static void main(String[] args) {

        try {
            ScheduledExecutorService timer = Executors.newScheduledThreadPool(3);

            CommandLine commandLine = (new DefaultParser()).parse(OPTIONS, args);
            if (HELP(commandLine)) {
                return;
            }

            Util.updateLogLevel(getValue(commandLine, LOGLEVEL, s -> s, "INFO"));

            final int connections = getValue(commandLine, CONNECTIONS, Integer::parseInt, 1);
            final long connectInterval = getValue(commandLine, CONNECTINTERVAL, Long::parseLong, 2L);
            log.info("connections {} , connectInterval {} .", connections, connectInterval);

            final boolean subscribe = hasOption(commandLine, SUBSCRIBE);
            final String subscribeString = getValue(commandLine, SUBSCRIBE, s -> s, null);
            final long subscribeInterval = getValue(commandLine, SUBSCRIBEINTERVAL, Long::parseLong, 2L);
            log.info("subscribe {} , subscribeString {} subscribeInterval {} .", subscribe, subscribeString, subscribeInterval);

            final boolean heartBeat = hasOption(commandLine, HEARTBEAT);
            final String heartBeatString = getValue(commandLine, HEARTBEAT, s -> s, null);
            final int heartBeatInterval = getValue(commandLine, HEARTBEATINTERVAL, Integer::parseInt, 15);
            log.info("heartBeat {} , heartBeatString {} heartBeatInterval {} .", heartBeat, heartBeatString, heartBeatInterval);

            if (subscribe) {
                timer.scheduleAtFixedRate(() -> ChannelManager.subscribe(subscribeString, subscribeInterval), 3, 5, TimeUnit.SECONDS);
            }
            if (heartBeat) {
                timer.scheduleAtFixedRate(() -> ChannelManager.heartBeat(heartBeatString), 1, heartBeatInterval, TimeUnit.SECONDS);
            }
            timer.scheduleAtFixedRate(() -> ChannelManager.monitor(), 5, 10, TimeUnit.SECONDS);

            final ClientConfig clientConfig = ClientConfig.builder()
                    .schema(hasOption(commandLine, SSL) ? WebSocketSchema.WSS : WebSocketSchema.WS)
                    .host(getValue(commandLine, HOST, s -> s, "127.0.0.1"))
                    .port(getValue(commandLine, PORT, Integer::parseInt, 8000))
                    .path(getValue(commandLine, PATH, s -> s, "/"))
                    .unCompressGzip(hasOption(commandLine, UNGZIPBIN))
                    .unCompressJSON(hasOption(commandLine, UNGZIPJSON))
                    .build();
            log.info(clientConfig.toString());

            final NettyClient client = new NettyClient(clientConfig);
            for (int i = 0; i < connections; i++) {
                client.connect();
                Util.sleepMS(connectInterval);
            }
        } catch (Exception e) {
            log.error("Client Start Error : ", e);
            Validate.isTrue(false);
            System.exit(0);
        }
    }
}
