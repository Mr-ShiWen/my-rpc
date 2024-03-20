package com.sw.rpc.assist.loadbalance.impl;

import com.sw.rpc.assist.loadbalance.AbstractLoadBalance;
import com.sw.rpc.dto.detail.RpcRequest;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
