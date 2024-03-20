package com.sw.rpc.dto.detail;

import com.sw.rpc.dto.Message;

import java.io.Serializable;

public class Ping extends Message implements Serializable {
    @Override
    public int messageType() {
        return PING_MESSAGE;
    }
}
