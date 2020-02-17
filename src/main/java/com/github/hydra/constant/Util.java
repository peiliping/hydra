package com.github.hydra.constant;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;


@Slf4j
public class Util {


    public static final String DEMO_NAMESPACE = "demo";

    public static final String DEMO_UID = "demo";


    public static void sleepSec(long sec) {

        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            log.error("Util.sleep", e);
        }
    }


    public static void sleepMS(long ms) {

        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            log.error("Util.sleep", e);
        }
    }


    public static long nowSec() {

        return System.currentTimeMillis() / 1000;
    }


    public static long nowMS() {

        return System.currentTimeMillis();
    }


    public static void updateLogLevel(String level) {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("root");
        rootLogger.setLevel(Level.toLevel(level));
    }
}
