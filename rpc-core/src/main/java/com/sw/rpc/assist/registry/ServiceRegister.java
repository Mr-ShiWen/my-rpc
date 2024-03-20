package com.sw.rpc.assist.registry;

import java.net.InetSocketAddress;

public interface ServiceRegister {

    void registerService(String serviceName, InetSocketAddress address) throws Exception;
}
