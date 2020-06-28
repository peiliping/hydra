package com.github.hydra.constant;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


@Slf4j
public class Util {


    public final static long MIN_1 = 60 * 1000L;

    public final static long SEC_45 = 45 * 1000L;

    private static final String SEP = "_";


    public static void updateLogLevel(String level) {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("root");
        rootLogger.setLevel(Level.toLevel(level));
    }


    public static void sleepMS(long ms) {

        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            log.error("Util.sleep", e);
        }
    }


    public static void sleepSec(long sec) {

        if (sec <= 0) {
            return;
        }
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            log.error("Util.sleep", e);
        }
    }


    public static long nowMS() {

        return System.currentTimeMillis();
    }


    public static long nowSec() {

        return System.currentTimeMillis() / 1000;
    }


    public static byte[] compressGzip(String str) {

        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes("UTF-8"));
            gzip.close();
        } catch (IOException e) {
            log.error("gzip compress error : ", e);
        }
        return out.toByteArray();
    }


    public static String unCompressGzip(byte[] bytes) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            GZIPInputStream unGzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = unGzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toString();
        } catch (Exception e) {
            log.error("un compress gzip error : ", e);
        }
        return "";
    }


    public static String buildNameSpace(String... keys) {

        return StringUtils.joinWith(SEP, keys);
    }
}
