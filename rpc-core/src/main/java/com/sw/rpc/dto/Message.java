package com.sw.rpc.dto;

import com.sw.rpc.dto.detail.Ping;
import com.sw.rpc.dto.detail.Pong;
import com.sw.rpc.dto.detail.RpcRequest;
import com.sw.rpc.dto.detail.RpcResponse;
import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;

@Data
public abstract class Message {
    public static final int RPC_REQUEST_MESSAGE = 1;
    public static final int RPC_RESPONSE_MESSAGE = 2;
    public static final int PING_MESSAGE = 3;
    public static final int PONG_MESSAGE = 4;

    private String requestId;

    public abstract int messageType();

    public static final ConcurrentHashMap<Integer,Class<?>> messageClassMap=new ConcurrentHashMap<>();
    static {
        messageClassMap.put(RPC_REQUEST_MESSAGE, RpcRequest.class);
        messageClassMap.put(RPC_RESPONSE_MESSAGE, RpcResponse.class);
        messageClassMap.put(PING_MESSAGE, Ping.class);
        messageClassMap.put(PONG_MESSAGE, Pong.class);
    }


}
