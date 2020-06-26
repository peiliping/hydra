package com.github.hydra.client;


import com.github.hydra.constant.WebSocketSchema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;


@Builder
@Getter
@ToString
public class ClientConfig {


    private WebSocketSchema schema;

    private String host;

    private int port;

    private String path;

    private boolean unCompressGzip;

    private boolean unCompressJSON;
}
