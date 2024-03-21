package com.sw.servertest;

import com.sw.rpc.core.server.RpcServer;
import com.sw.servertest.services.HelloServiceImplDefault;
import com.sw.servertest.services.HelloServiceImplTony;


public class TestNoSpring {
    public static void main(String[] args) {
        // 创建 rpcServer
        RpcServer rpcServer = new RpcServer();
        // 添加服务
        rpcServer.addServiceWithClass(HelloServiceImplTony.class);
        rpcServer.addServiceWithClass(HelloServiceImplDefault.class);
        // 启动 rpcServer
        rpcServer.start();
    }
}
