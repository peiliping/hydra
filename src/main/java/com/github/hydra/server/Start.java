package com.github.hydra.server;


import com.github.hydra.constant.Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.lang3.Validate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.hydra.constant.CMDUtil.getValue;
import static com.github.hydra.constant.CMDUtil.hasOption;
import static com.github.hydra.server.CMD.*;


@Slf4j
public class Start {


    private static ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);


    public static void main(String[] args) {

        try {
            CommandLine commandLine = (new DefaultParser()).parse(OPTIONS, args);
            if (HELP(commandLine)) {
                return;
            }

            Util.updateLogLevel(getValue(commandLine, LOGLEVEL, s -> s, "INFO"));

            new Producer(getValue(commandLine, REDIS_ADDRESS, s -> s, "redis://127.0.0.1:6379"),
                         getValue(commandLine, REDIS_PWD, s -> s, null),
                         getValue(commandLine, REDIS_DBNUM, Integer::parseInt, 0),
                         getValue(commandLine, REDIS_TOPIC, s -> s, "hydra")).start();

            final int monitorInterval = getValue(commandLine, MONITOR_INTERVAL, Integer::parseInt, 10);
            log.info("monitorInterval {} .", monitorInterval);
            final boolean checkIdle = hasOption(commandLine, CHECK_IDLE);
            log.info("checkIdle {} .", checkIdle);

            timer.scheduleAtFixedRate(() -> ChannelManager.monitorLog(checkIdle), monitorInterval, monitorInterval, TimeUnit.SECONDS);

            final ServerConfig serverConfig = ServerConfig.builder()
                    .host(getValue(commandLine, HOST, s -> s, "127.0.0.1"))
                    .port(getValue(commandLine, PORT, Integer::parseInt, 8000))
                    .path(getValue(commandLine, PATH, s -> s, "/")).build();
            log.info("Server Config : {} .", serverConfig.toString());

            new NettyServer(serverConfig).start();
        } catch (Throwable e) {
            log.error("Server Start Error : ", e);
            Validate.isTrue(false);
            System.exit(0);
        }
    }
}
