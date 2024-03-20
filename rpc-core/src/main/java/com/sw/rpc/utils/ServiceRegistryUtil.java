package com.sw.rpc.utils;

public class ServiceRegistryUtil {
    public static String constructServiceName(String intefaceName, String implName) {
        return intefaceName + "#" + implName;
    }

    public static String constructServiceMethodName(String intefaceName, String implName, String methodName) {
        return intefaceName + "#" + implName + "." + methodName;
    }

    public static String getServiceNameFromServiceImplClass(Class<?> serviceImplClass){
        // 1.获取接口名
        String interfaceName = InterfaceUtil.getInterfaceName(serviceImplClass);

        // 2.获取实现名
        String implName = AnnotationInfoUtil.getImplementNameForRpcServerInfo(serviceImplClass);

        // 3.生成服务名
        return constructServiceName(interfaceName, implName);
    }
}
