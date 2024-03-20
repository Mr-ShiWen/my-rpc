package com.sw.rpc.assist.serialize;

import com.sw.rpc.utils.JsonUtil;

import java.io.*;

/**
 * @author sw
 */
public interface Serializer {
    <T> T deserialize(Class<T> clazz, byte[] bytes);

    <T> byte[] serialize(T obj);

    enum Algorithm implements Serializer {
        JDK {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    return (T) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("JDK 反序列化失败", e);
                }
            }

            @Override
            public <T> byte[] serialize(T obj) {
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
                    oos.writeObject(obj);
                    return byteArrayOutputStream.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("JDK 序列化失败", e);
                }
            }
        },
        JSON {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                return JsonUtil.Unmarshal(clazz, new String(bytes));
            }

            @Override
            public <T> byte[] serialize(T obj) {
                return JsonUtil.MarshalToString(obj).getBytes();
            }
        }
    }


}
