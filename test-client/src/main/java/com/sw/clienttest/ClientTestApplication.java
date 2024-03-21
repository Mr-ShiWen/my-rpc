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
        // 运行启动类
        ConfigurableApplicationContext context = SpringApplication.run(ClientTestApplication.class, args);
        // 获取 rpcClient（也可以在业务 bean 里面注入属性）
        RpcClient rpcClient = context.getBean(RpcClient.class);
        // 获取代理
        HelloService helloServiceTony = rpcClient.getServerProxyWithServiceDiscover(HelloService.class, "Tony");
        HelloService helloServiceDefault = rpcClient.getServerProxyWithServiceDiscover(HelloService.class);
        // 执行 rpc
        for (int i = 0; i < 3; i++) {
            log.info(helloServiceTony.sayHello("Liming"));
        }
        for (int i = 0; i < 3; i++) {
            log.info(helloServiceDefault.sayHello("Liming"));
        }
    }

}
