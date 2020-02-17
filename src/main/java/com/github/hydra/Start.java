package com.github.hydra;


public class Start {


    public static void main(String[] args) {


        String base = "java -classpath hydra-1.0-SNAPSHOT.jar %s --help";

        System.out.println(String.format(base, com.github.hydra.server.Start.class.getCanonicalName()));
        System.out.println(String.format(base, com.github.hydra.client.Start.class.getCanonicalName()));
    }

}
