package com.sw.rpc.utils;

import com.sw.rpc.assist.annotation.RpcServerInfo;

import static com.sw.rpc.constant.Constant.DEFAULT_SERVICE_IMPL_NAME;

public class AnnotationInfoUtil {

    public static String getImplementNameForRpcServerInfo(Class<?> clazz) {
        RpcServerInfo rpcServerInfo = clazz.getAnnotation(RpcServerInfo.class);
        if (rpcServerInfo == null) {
            return DEFAULT_SERVICE_IMPL_NAME;
        }
        return rpcServerInfo.implName();
    }

}
