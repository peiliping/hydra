package com.github.hydra.server;


import com.github.hydra.constant.WebSocketSchema;
import io.netty.handler.logging.LogLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;


@Builder
@Getter
@ToString
public class ServerConfig {


    @Builder.Default
    private WebSocketSchema schema = WebSocketSchema.WS;

    @Builder.Default
    private String host = "127.0.0.1";

    @Builder.Default
    private int port = 8000;

    @Builder.Default
    public String path = "/";

    @Builder.Default
    private boolean epoll = false;

}
