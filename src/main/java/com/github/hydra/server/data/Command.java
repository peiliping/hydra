package com.github.hydra.server.data;


import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Getter
@Setter
public class Command {


    public static final String SUBSCRIBE = "sub";

    public static final String UNSUBSCRIBE = "unsub";

    public static final String PING = "ping";

    public static final String PONG = "pong";

    private String event;

    private String biz;

    private String type;

    private Set<String> topics;

    private String token;

}
