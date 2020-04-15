package com.github.hydra.server.data;


import com.github.hydra.constant.Util;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;


@Getter
@Builder
public class Answer {


    private String event;

    private String biz;

    private String type;

    private Set<String> topics;

    @Builder.Default
    private long timestamp = Util.nowSec();

    @Builder.Default
    private boolean success = true;

}
