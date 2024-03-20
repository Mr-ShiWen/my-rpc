package com.sw.rpc.utils;

import java.util.concurrent.atomic.AtomicLong;

public class UuidUtil {
    private static final AtomicLong atomicLong = new AtomicLong(0);

    public static String getRequestId() {
        long value = atomicLong.getAndIncrement();
        return String.valueOf(value);
    }
}
