package com.cleanroommc.bogosorter.compat.data_driven.utils;

import com.google.gson.JsonElement;

/**
 * @author ZZZank
 */
public class ReflectUtils {

    public static <T> Class<? extends T> toClass(JsonElement className, Class<T> filter) {
        return toClass(className.getAsString(), filter);
    }

    public static <T> Class<? extends T> toClass(String className, Class<T> filter) {
        return requireSubClassOf(toClass(className), filter);
    }

    public static Class<?> toClass(String className) {
        Class<?> c;
        try {
            c = Class.forName(className, false, ReflectUtils.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return c;
    }

    public static <T> Class<? extends T> requireSubClassOf(Class<?> c, Class<T> filter) {
        if (!filter.isAssignableFrom(c)) {
            throw new IllegalArgumentException(String.format("class '%s' is not a subclass of '%s'", c, filter.getName()));
        }
        return (Class<? extends T>) c;
    }
}
