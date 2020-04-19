package com.github.hydra.server.data;


import com.github.hydra.constant.Util;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class PushMsg {


    private BizType biz;

    private MsgType type;

    private String topic;

    private Object data;

    private boolean zip;

    @Builder.Default
    private long ts = Util.nowSec();
}
