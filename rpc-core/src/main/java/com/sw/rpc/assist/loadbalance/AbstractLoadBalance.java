package com.sw.rpc.assist.loadbalance;

import com.sw.rpc.dto.detail.RpcRequest;
import com.sw.rpc.utils.CollectionUtil;

import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (CollectionUtil.isEmpty(serviceAddresses)) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);

    public enum LoadBalanceType {
        LOADBALANCETYPE_POLLING,
        LOADBALANCETYPE_RANDOM,
        LOADBALANCETYPE_CONSISTENT
    }
}
