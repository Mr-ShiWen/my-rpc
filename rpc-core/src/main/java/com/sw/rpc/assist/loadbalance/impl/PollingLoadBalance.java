package com.sw.rpc.assist.loadbalance.impl;

import com.sw.rpc.assist.loadbalance.AbstractLoadBalance;
import com.sw.rpc.dto.detail.RpcRequest;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sw.rpc.utils.ServiceRegistryUtil.constructServiceMethodName;

public class PollingLoadBalance extends AbstractLoadBalance {
    private ConcurrentHashMap<String, RollingSelector> selectorMap = new ConcurrentHashMap<>();

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        String perfMethodName = constructServiceMethodName(rpcRequest.getInterfaceName(), rpcRequest.getImplName(), rpcRequest.getMethodName());
        RollingSelector rollingSelector = selectorMap.getOrDefault(perfMethodName, null);

        if (rollingSelector == null || rollingSelector.serviceAddresses != serviceAddresses) {
            synchronized (this) {
                rollingSelector = selectorMap.getOrDefault(perfMethodName, null);
                if (rollingSelector == null || rollingSelector.serviceAddresses != serviceAddresses) {
                    rollingSelector = new RollingSelector(serviceAddresses);
                    selectorMap.put(perfMethodName, rollingSelector);
                }
            }
        }

        return rollingSelector.select();
    }


    class RollingSelector {
        private final List<String> serviceAddresses;

        private AtomicInteger nextIndex;

        // 线程安全,因为不同线程有先后轮询的约束
        public String select() {
            int curIndex = nextIndex.getAndIncrement();
            curIndex=curIndex%serviceAddresses.size();
            return serviceAddresses.get(curIndex);
        }

        public RollingSelector(List<String> serviceAddresses) {
            this.serviceAddresses = serviceAddresses;
            this.nextIndex = new AtomicInteger(0);
        }
    }
}
