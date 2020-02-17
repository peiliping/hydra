package com.github.hydra.server;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Collection;


public class CMD {


    static final Options OPTIONS = new Options();


    static final Option HELP = Option.builder().longOpt("help").hasArg(false).required(false).desc("help").build();

    static final Option LOGLEVEL = Option.builder().longOpt("logLevel").hasArg(true).required(false).desc("root logger级别").build();


    static final Option PUSHINTERVAL = Option.builder().longOpt("pushInterval").hasArg(true).required(false).desc("推送间隔").build();


    static final Option SSL = Option.builder().longOpt("ssl").hasArg(false).required(false).desc("使用ssl").build();

    static final Option HOST = Option.builder().longOpt("host").hasArg(true).required(false).desc("ip or address").build();

    static final Option PORT = Option.builder().longOpt("port").hasArg(true).required(false).desc("port").build();

    static final Option PATH = Option.builder().longOpt("path").hasArg(true).required(false).desc("path").build();

    static {
        OPTIONS.addOption(HELP).addOption(LOGLEVEL)
                .addOption(PUSHINTERVAL)
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
