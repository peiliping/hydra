package com.github.hydra.server;


import com.alibaba.fastjson.JSON;
import com.github.hydra.server.data.Command;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;


public class CmdTest {


    @Test
    public void T_Sub() {

        Command cmd = new Command();
        cmd.setEvent(Command.SUBSCRIBE);
        cmd.setBiz("biz");
        cmd.setType("type");
        cmd.setTopics(Sets.newHashSet("a", "b"));

        System.out.println(JSON.toJSONString(cmd));
    }


    @Test
    public void T_Producer_Data() {

        Map<String, Object> data = Maps.newHashMap();
        data.put("biz", "biz");
        data.put("type", "type");
        data.put("topic", "a");
        data.put("data", Arrays.asList(1, 2, 3));

        System.out.println(JSON.toJSONString(data));
    }
}
