package com.github.hydra.client.data;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class PushMsg {


    private Object data;

    private boolean zip;

}
