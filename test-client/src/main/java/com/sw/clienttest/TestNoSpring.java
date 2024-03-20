package com.sw.clienttest;

import com.sw.api.HelloService;
import com.sw.rpc.core.client.RpcClient;
import lombok.extern.slf4j.Slf4j;

import static com.sw.rpc.assist.loadbalance.AbstractLoadBalance.LoadBalanceType.LOADBALANCETYPE_POLLING;

@Slf4j
public class TestNoSpring {

    @org.junit.jupiter.api.Test
    public void testCallWithServerDiscover() {
        RpcClient rpcClient = new RpcClient(LOADBALANCETYPE_POLLING);
        HelloService helloServiceLily = rpcClient.getServerProxyWithServiceDiscover(HelloService.class, "Lily");
        HelloService helloServiceTony = rpcClient.getServerProxyWithServiceDiscover(HelloService.class, "Tony");
        for (int i = 0; i < 6; i++) {
            log.info(helloServiceLily.sayHello("shiwen"));
        }
        for (int i = 0; i < 6; i++) {
            log.info(helloServiceTony.sayHello("shiwen"));
        }
    }

    @org.junit.jupiter.api.Test
    public void testCallWithSpecificConn() {
        RpcClient rpcClient = new RpcClient(LOADBALANCETYPE_POLLING);
        HelloService helloServiceLily = rpcClient.getServerProxyWithSpecificConn(HelloService.class, "192.168.1.3", 8080, "Lily");
        HelloService helloServiceTony = rpcClient.getServerProxyWithSpecificConn(HelloService.class, "192.168.1.3", 8080, "Tony");
        for (int i = 0; i < 6; i++) {
            log.info(helloServiceLily.sayHello("Liming"));
        }
        for (int i = 0; i < 6; i++) {
            log.info(helloServiceTony.sayHello("Liming"));
        }
    }
}
