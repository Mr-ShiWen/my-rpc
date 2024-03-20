package com.sw.rpc.assist.registry.impl;

import com.sw.rpc.config.LocalConfig;
import com.sw.rpc.dto.detail.RpcRequest;
import com.sw.rpc.assist.registry.ServiceDiscover;
import com.sw.rpc.utils.ServiceRegistryUtil;
import com.sw.rpc.zookeeper.ZKClient;

import java.io.IOException;
import java.util.List;

public class ServiceDiscoverImpl implements ServiceDiscover {

    private ZKClient myZkClient;

    private String rootPath;


    @Override
    public List<String> discoverService(RpcRequest rpcRequest) throws Exception {
        String serviceName = ServiceRegistryUtil.constructServiceName(rpcRequest.getInterfaceName(), rpcRequest.getImplName());
        String servicePath = rootPath + "/" + serviceName;
        return myZkClient.getChildrenOfPath(servicePath);
    }

    public ServiceDiscoverImpl(String connections, int sessionTimeOut, String rootPath) throws IOException {
        this.rootPath = rootPath;
        myZkClient = new ZKClient(connections, sessionTimeOut);
    }
}

