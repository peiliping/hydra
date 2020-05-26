package com.github.hydra.client;


import com.github.hydra.constant.Util;
import com.github.hydra.constant.WebSocketSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.hydra.client.CMD.*;
import static com.github.hydra.constant.CMDUtil.getValue;
import static com.github.hydra.constant.CMDUtil.hasOption;


@Slf4j
public class Start {


    private static ScheduledExecutorService timer = Executors.newScheduledThreadPool(2);


    public static void main(String[] args) {

        try {
            CommandLine commandLine = (new DefaultParser()).parse(OPTIONS, args);
            if (HELP(commandLine)) {
                return;
            }

            Util.updateLogLevel(getValue(commandLine, LOGLEVEL, s -> s, "INFO"));

            final int connections = getValue(commandLine, CONNECTIONS, Integer::parseInt, 1);
            final long connectInterval = getValue(commandLine, CONNECTINTERVAL, Long::parseLong, 3L);
            log.info("connections {} , connectInterval {} .", connections, connectInterval);


            final boolean subscribe = hasOption(commandLine, SUBSCRIBE);
            final String subscribeString = getValue(commandLine, SUBSCRIBE, s -> s, null);
            log.info("subscribe {} , subscribeString {} .", subscribe, subscribeString);

            final boolean heartBeat = hasOption(commandLine, HEARTBEAT);
            final String heartBeatString = getValue(commandLine, HEARTBEAT, s -> s, null);
            log.info("heartBeat {} , heartBeatString {} .", heartBeat, heartBeatString);

            timer.scheduleAtFixedRate(() -> ChannelManager.scan(heartBeat, heartBeatString), 1, 10, TimeUnit.SECONDS);
            if (subscribe) {
                timer.scheduleAtFixedRate(() -> ChannelManager.subscribe(subscribeString, connectInterval), 6, 10, TimeUnit.SECONDS);
            }

            final ClientConfig clientConfig = ClientConfig.builder()
                    .schema(hasOption(commandLine, SSL) ? WebSocketSchema.WSS : WebSocketSchema.WS)
                    .host(getValue(commandLine, HOST, s -> s, "127.0.0.1"))
                    .port(getValue(commandLine, PORT, Integer::parseInt, 8000))
                    .path(getValue(commandLine, PATH, s -> s, "/"))
                    .unCompressGzip(hasOption(commandLine, UNGZIP))
                    .build();
            log.info(clientConfig.toString());

            final NettyClient client = new NettyClient(clientConfig);
            for (int i = 0; i < connections; i++) {
                client.connect();
                Util.sleepMS(connectInterval);
            }
        } catch (Exception e) {
            log.error("Client Start Error : ", e);
        }
    }
}
