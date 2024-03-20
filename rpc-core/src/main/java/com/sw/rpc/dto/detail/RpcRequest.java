package com.sw.rpc.dto.detail;

import com.sw.rpc.dto.Message;
import lombok.Data;

import java.io.Serializable;

import static com.sw.rpc.constant.Constant.RpcVersion.RPC_VERSION_1;

@Data
public class RpcRequest extends Message implements Serializable {
    // 请求信息
    private String interfaceName;
    private String methodName;
    private String[] paramTypes;
    private String returnType;
    private Object[] params;
    private String implName;
    // rpc 请求本身的 version
    private int version = RPC_VERSION_1.getVersion();


    public RpcRequest(String requestId, String interfaceName, String methodName, String[] paramTypes, String returnType, Object[] params, String implName) {
        super.setRequestId(requestId);
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.params = params;
        this.implName = implName;

        this.version = version;
    }

    public RpcRequest() {
    }

    @Override
    public int messageType() {
        return RPC_REQUEST_MESSAGE;
    }
}
