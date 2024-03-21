package com.sw.servertest.services;

import com.sw.api.HelloService;
import com.sw.rpc.assist.annotation.RpcServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RpcServerInfo
public class HelloServiceImplDefault implements HelloService {
    private String name = "default";

    @Override
    public String sayHello(String friendName) {
        log.info("enter Lily sayHello");
        return "hello " + friendName + ", my name is " + name + ", nice to meet you";
    }
}
