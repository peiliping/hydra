package com.github.hydra.client;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Collection;


public class CMD {


    static final Options OPTIONS = new Options();


    static final Option HELP = Option.builder().longOpt("help").hasArg(false).required(false).desc("help").build();

    static final Option LOGLEVEL = Option.builder().longOpt("logLevel").hasArg(true).required(false).desc("root logger级别").build();


    static final Option CONNECTIONS = Option.builder().longOpt("connections").hasArg(true).required(false).desc("连接数").build();

    static final Option CONNECTINTERVAL = Option.builder().longOpt("connectInterval").hasArg(true).required(false).desc("connect间隔").build();

    static final Option SUBSCRIBE = Option.builder().longOpt("subscribe").hasArg(true).required(false).desc("订阅请求的字符串").build();

    static final Option HEARTBEAT = Option.builder().longOpt("heartBeat").hasArg(true).required(false).desc("发送心跳包的字符串,时间戳用%s占位").build();

    static final Option PARSERESULT = Option.builder().longOpt("parseResult").hasArg(false).required(false).desc("解析server返回结果").build();


    static final Option SSL = Option.builder().longOpt("ssl").hasArg(false).required(false).desc("使用ssl").build();

    static final Option HOST = Option.builder().longOpt("host").hasArg(true).required(false).desc("ip or address").build();

    static final Option PORT = Option.builder().longOpt("port").hasArg(true).required(false).desc("port").build();

    static final Option PATH = Option.builder().longOpt("path").hasArg(true).required(false).desc("path").build();

    static {
        OPTIONS.addOption(HELP).addOption(LOGLEVEL)
                .addOption(CONNECTIONS).addOption(CONNECTINTERVAL)
                .addOption(SUBSCRIBE).addOption(HEARTBEAT).addOption(PARSERESULT)
                .addOption(SSL).addOption(HOST).addOption(PORT).addOption(PATH);
    }

    public static boolean HELP(CommandLine commandLine) {

        if (!commandLine.hasOption(HELP.getLongOpt())) {
            return false;
        }
        Collection<Option> collection = OPTIONS.getOptions();
        System.out.println(String.format("%-15s %-7s %-8s %-10s", "Name", "HasArg", "Required", "Description"));
        for (Option op : collection) {
            String c = String.format("%-15s %-7s %-8s %-10s", op.getLongOpt(), op.hasArg(), op.isRequired(), op.getDescription());
            System.out.println(c);
        }
        return true;
    }
}
