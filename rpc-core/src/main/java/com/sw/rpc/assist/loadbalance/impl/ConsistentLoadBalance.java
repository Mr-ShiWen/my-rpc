package com.sw.rpc.assist.loadbalance.impl;

import com.sw.rpc.assist.loadbalance.AbstractLoadBalance;
import com.sw.rpc.dto.detail.RpcRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.sw.rpc.utils.ServiceRegistryUtil.constructServiceMethodName;

public class ConsistentLoadBalance extends AbstractLoadBalance {
    private ConcurrentHashMap<String, ConsistentSelector> consistentSelectorMap = new ConcurrentHashMap<>();


    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        String perfMethodName = constructServiceMethodName(rpcRequest.getInterfaceName(), rpcRequest.getImplName(), rpcRequest.getMethodName());
        ConsistentSelector consistentSelector = consistentSelectorMap.getOrDefault(perfMethodName, null);
        if (consistentSelector == null || consistentSelector.serviceAddresses != serviceAddresses) {
            synchronized (this) {
                consistentSelector = consistentSelectorMap.getOrDefault(perfMethodName, null);
                if (consistentSelector == null || consistentSelector.serviceAddresses != serviceAddresses) {
                    consistentSelector = new ConsistentSelector(160, serviceAddresses);
                    consistentSelectorMap.put(perfMethodName, consistentSelector);
                }
            }
        }

        return consistentSelector.select(perfMethodName, rpcRequest.getParams());
    }

    class ConsistentSelector {
        private final TreeMap<Long, String> virtualServices;

        private final int replicaNumber;

        private final List<String> serviceAddresses;

        public ConsistentSelector(int replicaNumber, List<String> serviceAddresses) {
            this.virtualServices = new TreeMap<>();
            this.replicaNumber = replicaNumber;
            this.serviceAddresses = serviceAddresses;

            // 把服务地址哈希到环上
            for (String service : serviceAddresses) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(service + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualServices.put(m, service);
                    }
                }
            }
        }

        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }

        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        public String select(String perfMethodName, Object[] args) {
            StringBuilder sb = new StringBuilder();
            Arrays.stream(args).forEach(c -> sb.append(c.toString()));

            byte[] digest = md5(sb.toString());
            return selectForKey(hash(digest, 0));
        }

        public String selectForKey(long hashCode) {
            Map.Entry<Long, String> entry = virtualServices.ceilingEntry(hashCode);
            if (entry == null) {
                entry = virtualServices.firstEntry();
            }
            return entry.getValue();
        }
    }


}
