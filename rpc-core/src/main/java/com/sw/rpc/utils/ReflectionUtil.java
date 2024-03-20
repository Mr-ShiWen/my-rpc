package com.sw.rpc.utils;

public class ReflectionUtil {
    public static Class<?>[] getClassesForNames(String[] names) {
        if (CollectionUtil.isEmpty(names)) {
            return new Class[0];
        }
        Class<?>[] classes = new Class[names.length];
        for (int i = 0; i < classes.length; i++) {
            try {
                Class<?> aClass = Class.forName(names[i]);
                classes[i] = aClass;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return classes;
    }

    public static String[] getNamesForClasses(Class<?>[] classes) {
        if (CollectionUtil.isEmpty(classes)) {
            return new String[0];
        }
        String[] names = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            names[i] = classes[i].getName();
        }
        return names;
    }

    public static Class<?> getClassForName(String name) {
        try {
            Class<?> aClass = Class.forName(name);
            return aClass;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
