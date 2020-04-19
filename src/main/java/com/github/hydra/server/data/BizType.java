package com.github.hydra.server.data;


public enum BizType {

    NAMESPCACE("namespace"),
    USER("user"),
    BROADCAST("broadcast");

    public final String name;


    BizType(String name) {

        this.name = name;
    }


    public static BizType of(String s) {

        switch (s) {
            case "namespace":
                return NAMESPCACE;
            case "user":
                return USER;
            case "broadcast":
                return BROADCAST;
            default:
                return null;
        }
    }
}
