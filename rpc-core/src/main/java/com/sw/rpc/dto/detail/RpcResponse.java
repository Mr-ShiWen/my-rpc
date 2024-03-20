package com.sw.rpc.dto.detail;

import com.sw.rpc.dto.Message;
import lombok.Data;

import java.io.Serializable;

@Data
public class RpcResponse extends Message implements Serializable {
    // 成功时返回值
    private Object returnValue;
    // 0表示请求成功，其他表示失败
    private String errMsg;

    // rpc 请求本身的 version
    private int version = 1;

    public RpcResponse(String requestId, Object returnValue, String errMsg) {
        super.setRequestId(requestId);
        this.returnValue = returnValue;
        this.errMsg= errMsg;
    }

    public RpcResponse() {
    }

    @Override
    public int messageType() {
        return RPC_RESPONSE_MESSAGE;
    }
}
