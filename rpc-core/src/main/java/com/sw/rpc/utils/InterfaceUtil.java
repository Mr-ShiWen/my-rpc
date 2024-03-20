package com.sw.rpc.utils;

public class InterfaceUtil {

    public static String getInterfaceName(Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces == null || interfaces.length == 0) {
            return "";
        }
        return interfaces[0].getName();
    }
}
