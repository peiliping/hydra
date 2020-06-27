package com.github.hydra.client;


import com.github.hydra.constant.CMDUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;


public class CMD extends CMDUtil {


    static final Options OPTIONS = new Options();

    static final Option SSL = Option.builder().longOpt("ssl").hasArg(false).required(false).desc("使用ssl (false)").build();

    static final Option CONNECTIONS = Option.builder().longOpt("connections").hasArg(true).required(false).desc("创建连接数 (1)").build();

    static final Option CONNECTINTERVAL = Option.builder().longOpt("connectInterval").hasArg(true).required(false).desc("创建连接的间隔时间 (2ms)").build();

    static final Option SUBSCRIBE = Option.builder().longOpt("subscribe").hasArg(true).required(false).desc("订阅请求的字符串 (null)").build();

    static final Option SUBSCRIBEINTERVAL = Option.builder().longOpt("subscribeInterval").hasArg(true).required(false).desc("订阅请求的间隔时间 (2ms)").build();

    static final Option HEARTBEAT = Option.builder().longOpt("heartBeat").hasArg(true).required(false).desc("发送心跳包的字符串,时间戳用%s占位 (null)").build();

    static final Option HEARTBEATINTERVAL = Option.builder().longOpt("heartBeatInterval").hasArg(true).required(false).desc("心跳的间隔时间 (15s)").build();

    static final Option UNGZIPBIN = Option.builder().longOpt("unGzipBin").hasArg(false).required(false).desc("unGzip binary data (false)").build();

    static final Option UNGZIPJSON = Option.builder().longOpt("unGzipJson").hasArg(false).required(false).desc("unGzip json data (false)").build();

    static {
        OPTIONS.addOption(HELP).addOption(LOGLEVEL)
                .addOption(SSL).addOption(HOST).addOption(PORT).addOption(PATH)
                .addOption(CONNECTIONS).addOption(CONNECTINTERVAL)
                .addOption(SUBSCRIBE).addOption(SUBSCRIBEINTERVAL)
                .addOption(HEARTBEAT).addOption(HEARTBEATINTERVAL)
                .addOption(UNGZIPBIN).addOption(UNGZIPJSON);
    }

    public static boolean HELP(CommandLine commandLine) {

        return HELP(commandLine, OPTIONS);
    }
}
