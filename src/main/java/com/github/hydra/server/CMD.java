package com.github.hydra.server;


import com.github.hydra.constant.CMDUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;


public class CMD extends CMDUtil {


    static final Options OPTIONS = new Options();

    static final Option MONITOR_INTERVAL = Option.builder().longOpt("monitor-interval").hasArg(true).required(false).desc("monitor interval").build();

    static final Option CHECK_IDLE = Option.builder().longOpt("check-idle").hasArg(false).required(false).desc("check idle").build();

    static final Option REDIS_ADDRESS = Option.builder().longOpt("redis-address").hasArg(true).required(false).desc("redis address").build();

    static final Option REDIS_PWD = Option.builder().longOpt("redis-pwd").hasArg(true).required(false).desc("redis pwd").build();

    static final Option REDIS_DBNUM = Option.builder().longOpt("redis-dbnum").hasArg(true).required(false).desc("redis dbnum").build();

    static final Option REDIS_TOPIC = Option.builder().longOpt("redis-topic").hasArg(true).required(false).desc("redis topic").build();

    static {
        OPTIONS.addOption(HELP).addOption(LOGLEVEL).addOption(MONITOR_INTERVAL).addOption(CHECK_IDLE)
                .addOption(HOST).addOption(PORT).addOption(PATH)
                .addOption(REDIS_ADDRESS).addOption(REDIS_PWD).addOption(REDIS_DBNUM).addOption(REDIS_TOPIC);
    }

    public static boolean HELP(CommandLine commandLine) {

        return HELP(commandLine, OPTIONS);
    }
}
