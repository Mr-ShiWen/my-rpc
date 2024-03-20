package com.sw.rpc.dto.detail;

import com.sw.rpc.dto.Message;

import java.io.Serializable;

public class Pong extends Message implements Serializable {
    @Override
    public int messageType() {
        return PONG_MESSAGE;
    }
}
