package com.sw.rpc.assist.serialize;

import java.util.concurrent.ConcurrentHashMap;

public class SerializeHelper {
    public static final int SERIALIZE_TYPE_JDK = 1;
    public static final int SERIALIZE_TYPE_JSON = 2;

    private static final ConcurrentHashMap<Integer, Serializer> serializerMap = new ConcurrentHashMap<>();

    static {
        serializerMap.put(SERIALIZE_TYPE_JDK, Serializer.Algorithm.JDK);
        serializerMap.put(SERIALIZE_TYPE_JSON, Serializer.Algorithm.JSON);
    }

    public static <T> T deserialize(int serializeType, Class<T> clazz, byte[] bytes) {
        Serializer serializer = serializerMap.get(serializeType);
        return serializer.deserialize(clazz, bytes);
    }

    public static <T> byte[] serialize(int serializeType, T obj) {
        Serializer serializer = serializerMap.get(serializeType);
        return serializer.serialize(obj);
    }
}
