package com.sw.rpc.assist.loadbalance;

import com.sw.rpc.dto.detail.RpcRequest;

import java.util.List;

public interface LoadBalance {
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
