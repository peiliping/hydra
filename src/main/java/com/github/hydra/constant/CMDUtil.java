package com.github.hydra.constant;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Collection;


public class CMDUtil {


    public static final Option HELP = Option.builder().longOpt("help").hasArg(false).required(false).desc("help").build();

    public static final Option LOGLEVEL = Option.builder().longOpt("logLevel").hasArg(true).required(false).desc("root logger level").build();


    public static <V> V getValue(CommandLine commandLine, Option option, Convert<V> convert, V defaultValue) {

        if (commandLine.hasOption(option.getLongOpt())) {
            return convert.eval(commandLine.getOptionValue(option.getLongOpt()));
        }
        return defaultValue;
    }


    public static boolean hasOption(CommandLine commandLine, Option option) {

        return commandLine.hasOption(option.getLongOpt());
    }


    public static boolean HELP(CommandLine commandLine, Options options) {

        if (!commandLine.hasOption(HELP.getLongOpt())) {
            return false;
        }
        Collection<Option> collection = options.getOptions();
        System.out.println(String.format("%-18s %-7s %-8s %-10s", "Name", "HasArg", "Required", "Description"));
        for (Option op : collection) {
            String c = String.format("%-18s %-7s %-8s %-10s", op.getLongOpt(), op.hasArg(), op.isRequired(), op.getDescription());
            System.out.println(c);
        }
        return true;
    }


    public interface Convert<V> {


        V eval(String s);
    }

}
