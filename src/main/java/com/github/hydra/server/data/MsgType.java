package com.github.hydra.server.data;


public enum MsgType {

    TICKER("ticker");

    public final String name;


    MsgType(String name) {

        this.name = name;
    }


    public static MsgType of(String s) {

        switch (s) {
            case "ticker":
                return TICKER;
            default:
                return null;
        }
    }
}
