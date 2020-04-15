package com.github.hydra.constant;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;


public class CMDUtil {


    public static <V> V getValue(CommandLine commandLine, Option option, Convert<V> convert, V defaultValue) {

        if (commandLine.hasOption(option.getLongOpt())) {
            return convert.eval(commandLine.getOptionValue(option.getLongOpt()));
        }
        return defaultValue;
    }


    public static boolean hasOption(CommandLine commandLine, Option option) {

        return commandLine.hasOption(option.getLongOpt());
    }


    public interface Convert<V> {


        V eval(String s);
    }

}
