package com.sw.rpc.utils;

import java.util.Collection;

public class CollectionUtil {
    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }
}
