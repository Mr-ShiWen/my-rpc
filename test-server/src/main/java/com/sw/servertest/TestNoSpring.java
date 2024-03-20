package com.sw.servertest;

import com.sw.rpc.core.server.RpcServer;
import com.sw.servertest.services.HelloServiceLily;
import com.sw.servertest.services.HelloServiceTony;


public class TestNoSpring {
    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServer();
        rpcServer.addServiceWithClass(HelloServiceTony.class);
        rpcServer.addServiceWithClass(HelloServiceLily.class);
        rpcServer.start();
    }
}
