package com.sw.servertest.services;

import com.sw.api.HelloService;
import com.sw.rpc.assist.annotation.RpcServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RpcServerInfo(implName = "Tony") // implName 表示实现名称，区分相同接口的多个实现；客户端可以通过指定 implName 来调用指定实现类
public class HelloServiceImplTony implements HelloService {
    private String name = "Tony";

    @Override
    public String sayHello(String friendName) {
        log.info("enter Tony sayHello");
        return "hello " + friendName + ", my name is " + name + ", nice to meet you";
    }
}
