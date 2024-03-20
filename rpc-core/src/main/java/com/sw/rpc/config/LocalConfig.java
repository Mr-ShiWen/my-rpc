package com.sw.rpc.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class LocalConfig {
    private static Properties properties;


    public static String getProperty(String key) {
        if (properties == null) {
            synchronized (LocalConfig.class) {
                if (properties == null) {
                    initProperties();
                }
            }
        }
        return properties.getProperty(key);
    }

    private static void initProperties() {
        try {
            InputStream in = LocalConfig.class.getClassLoader().getResourceAsStream("application.properties");
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            log.error("fail to init properties config");
            throw new RuntimeException(e);
        }
    }
}
