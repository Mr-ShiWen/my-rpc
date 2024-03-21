package com.sw.servertest;

import com.sw.rpc.core.server.RpcServer;
import com.sw.servertest.services.HelloServiceImplDefault;
import com.sw.servertest.services.HelloServiceImplTony;


public class TestNoSpring {
    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServer();
        rpcServer.addServiceWithClass(HelloServiceImplTony.class);
        rpcServer.addServiceWithClass(HelloServiceImplDefault.class);
        rpcServer.start();
    }
}
