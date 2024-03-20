package com.sw.rpc.assist.registry;

import com.sw.rpc.dto.detail.RpcRequest;

import java.util.List;

public interface ServiceDiscover {
    List<String> discoverService(RpcRequest rpcRequest) throws Exception;
}
