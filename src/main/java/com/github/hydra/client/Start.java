package com.github.hydra.client;


import com.github.hydra.constant.Util;
import com.github.hydra.constant.WebSocketSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import static com.github.hydra.client.CMD.*;


@Slf4j
public class Start {


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

            new Thread(() -> {
                while (true) {
                    Util.sleepSec(10);
                    ChannelManager.scan(1000L, subscribe, subscribeString, heartBeat, heartBeatString);
                }
            }).start();

            final ClientConfig clientConfig = ClientConfig.builder()
                    .schema(hasOption(commandLine, SSL) ? WebSocketSchema.WSS : WebSocketSchema.WS)
                    .host(getValue(commandLine, HOST, s -> s, "127.0.0.1"))
                    .port(getValue(commandLine, PORT, Integer::parseInt, 8000))
                    .path(getValue(commandLine, PATH, s -> s, "/"))
                    .parseResult(hasOption(commandLine, PARSERESULT))
                    .build();
            log.info(clientConfig.toString());

            final NettyClient client = new NettyClient(clientConfig);
            for (int i = 0; i < connections; i++) {
                client.connect();
                Util.sleepMS(connectInterval);
            }
        } catch (ParseException e) {
            log.error("Client Start Error : ", e);
        }
    }


    private static <V> V getValue(CommandLine commandLine, Option option, Convert<V> convert, V defaultValue) {

        if (commandLine.hasOption(option.getLongOpt())) {
            return convert.eval(commandLine.getOptionValue(option.getLongOpt()));
        }
        return defaultValue;
    }


    public static boolean hasOption(CommandLine commandLine, Option option) {

        return commandLine.hasOption(option.getLongOpt());
    }


    interface Convert<V> {


        V eval(String s);
    }
}
