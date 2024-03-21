package com.sw.clienttest;

import com.sw.api.HelloService;
import com.sw.rpc.core.client.RpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Conditional;

import java.util.Scanner;

@Slf4j
@SpringBootApplication
public class ClientTestApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ClientTestApplication.class, args);
        RpcClient rpcClient = context.getBean(RpcClient.class);
        HelloService helloServiceTony = rpcClient.getServerProxyWithServiceDiscover(HelloService.class, "Tony");
        HelloService helloServiceDefault = rpcClient.getServerProxyWithServiceDiscover(HelloService.class);
        for (int i = 0; i < 3; i++) {
            log.info(helloServiceTony.sayHello("Liming"));
        }
        for (int i = 0; i < 3; i++) {
            log.info(helloServiceDefault.sayHello("Liming"));
        }
    }

}
