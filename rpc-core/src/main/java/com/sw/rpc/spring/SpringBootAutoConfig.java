package com.sw.rpc.spring;

import com.sw.rpc.config.LocalConfig;
import com.sw.rpc.core.client.RpcClient;
import com.sw.rpc.core.server.RpcServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = "com.sw.rpc.spring")
public class SpringBootAutoConfig {

    @Bean
    public RpcClient getRpcClient() {
        String clientButton = LocalConfig.getProperty("spring.rpc.client");
        switch (clientButton) {
            case "off":
                return null;
            default:
                return new RpcClient();
        }
    }

    @Bean
    public RpcServer getRpcServer() {
        String serverButton = LocalConfig.getProperty("spring.rpc.server");
        switch (serverButton) {
            case "off":
                return null;
            default:
                RpcServer rpcServer = new RpcServer();
                rpcServer.start();
                return rpcServer;
        }
    }
}
