package com.github.hydra.constant;


import java.util.Set;


public class Command {


    public static final String SUBSCRIBE = "subscribe";

    public static final String UNSUBSCRIBE = "unsubscribe";

    public String type;

    public Set<String> topics;

    public String uid;

}
