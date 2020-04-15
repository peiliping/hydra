package com.github.hydra.server.data;


import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class PushMsg {


    private String biz;

    private String type;

    private String topic;

    private Object data;

    private long ts;
}
