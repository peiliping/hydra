package com.github.hydra.server.data;


import com.github.hydra.constant.Util;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class PushMsg {


    private String biz;

    private String type;

    private String topic;

    private Object data;

    @Builder.Default
    private long ts = Util.nowSec();
}
