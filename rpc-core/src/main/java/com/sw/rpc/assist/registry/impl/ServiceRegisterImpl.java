package com.sw.rpc.assist.registry.impl;

import com.sw.rpc.config.LocalConfig;
import com.sw.rpc.assist.registry.ServiceRegister;
import com.sw.rpc.zookeeper.ZKClient;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServiceRegisterImpl implements ServiceRegister {
    private ZKClient myZkClient;
    private String rootPath;

    /**
     * 注册服务（服务名=接口名.实现名）
     *
     * @param serviceName 服务名
     * @param address     具体地址
     * @throws Exception 失败异常
     */
    @Override
    public void registerService(String serviceName, InetSocketAddress address) throws Exception {
        // 注册到 zookeeper
        String servicePath = rootPath + "/" + serviceName;
        myZkClient.createPersistentPathIfNotExist(servicePath);
        String addressPath = servicePath + "/" + address.getHostName() + ":" + address.getPort();
        myZkClient.checkAndCreateTmpPath(addressPath);
    }

    public ServiceRegisterImpl(String connections, int sessionTimeOut, String rootPath) throws IOException {
        this.rootPath = rootPath;
        myZkClient = new ZKClient(connections, sessionTimeOut);
    }
}
