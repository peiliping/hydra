package com.github.hydra.server;


import com.alibaba.fastjson.JSON;
import com.github.hydra.constant.Command;
import com.github.hydra.constant.Result;
import com.github.hydra.constant.Util;
import com.github.hydra.constant.WebSocketSchema;
import com.google.common.collect.Sets;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import static com.github.hydra.server.CMD.*;


@Slf4j
public class Start {


    public static void main(String[] args) {

        try {
            CommandLine commandLine = (new DefaultParser()).parse(OPTIONS, args);
            if (HELP(commandLine)) {
                return;
            }

            Util.updateLogLevel(getValue(commandLine, LOGLEVEL, s -> s, "INFO"));

            printCMD();

            final long pushInterval = getValue(commandLine, PUSHINTERVAL, Long::parseLong, 1000L);
            log.info("pushInterval {} .", pushInterval);

            new Thread(() -> {

                while (true) {
                    Util.sleepSec(10);
                    ChannelManager.printLog();
                }
            }).start();

            new Thread(() -> {
                while (true) {
                    Result r = new Result();
                    r.timestamp = Util.nowMS();
                    r.success = true;
                    TextWebSocketFrame frame = new TextWebSocketFrame(JSON.toJSONString(r));
                    ChannelManager.broadCastInNameSpace(Util.DEMO_NAMESPACE, frame);
                    Util.sleepMS(pushInterval);
                }
            }).start();

            new Thread(() -> {
                while (true) {
                    Result r = new Result();
                    r.timestamp = Util.nowMS();
                    r.success = true;
                    ChannelManager.broadCast4User(Util.DEMO_UID, JSON.toJSONString(r));
                    Util.sleepMS(pushInterval);
                }
            }).start();

            final ServerConfig serverConfig = ServerConfig.builder()
                    .schema(hasOption(commandLine, SSL) ? WebSocketSchema.WSS : WebSocketSchema.WS)
                    .host(getValue(commandLine, HOST, s -> s, "127.0.0.1"))
                    .port(getValue(commandLine, PORT, Integer::parseInt, 8000))
                    .path(getValue(commandLine, PATH, s -> s, "/"))
                    .build();
            log.info(serverConfig.toString());

            new NettyServer(serverConfig).start();
        } catch (ParseException e) {
            log.error("Server Start Error : ", e);
        }
    }


    private static void printCMD() {

        Command commandSub = new Command();
        commandSub.type = Command.SUBSCRIBE;
        commandSub.topics = Sets.newHashSet(Util.DEMO_NAMESPACE);
        commandSub.uid = Util.DEMO_UID;
        log.info(JSON.toJSONString(commandSub));
        Command commandUnSub = new Command();
        commandUnSub.type = Command.UNSUBSCRIBE;
        log.info(JSON.toJSONString(commandUnSub));
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
