package com.github.hydra.server.data;


public enum BizType {

    EXCHANGE("exchange");

    public final String name;


    BizType(String name) {

        this.name = name;
    }


    public static BizType of(String s) {

        if (EXCHANGE.name.equals(s)) {
            return EXCHANGE;
        } else {
            return null;
        }
    }

}
