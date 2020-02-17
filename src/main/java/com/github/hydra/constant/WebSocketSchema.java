package com.github.hydra.constant;


public enum WebSocketSchema {

    WS("ws"), WSS("wss");

    public final String schema;


    WebSocketSchema(String schema) {

        this.schema = schema;
    }

}
