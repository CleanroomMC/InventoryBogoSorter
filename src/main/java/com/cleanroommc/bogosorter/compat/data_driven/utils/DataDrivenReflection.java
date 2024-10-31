package com.cleanroommc.bogosorter.compat.data_driven.utils;

import java.util.List;

/**
 * {@code accessName}
 *
 * @author ZZZank
 */
public class DataDrivenReflection {

    public static <T> UnsafeMapper<Object, T> compile(String accessKey, Object instance, Class<T> expectedType) {
        Class<?> c = instance.getClass();
        if (accessKey.endsWith("()")) {
            var actualKey = accessKey.substring(0, accessKey.length() - 2);
            var method = UnsafeMapper.memorize(ignored -> c.getMethod(actualKey));
            return inst -> expectedType.cast(method.map(null).invoke(inst));
        } else {
            var field = UnsafeMapper.memorize(ignored -> c.getField(accessKey));
            return inst -> expectedType.cast(field.map(null).get(inst));
        }
    }

    public static <T> T access(Object instance, String accessName, Class<T> expectedType, boolean returnNullOnFail) {
        Class<?> c = instance.getClass();
        try {
            Object result = accessName.endsWith("()")
                ? c.getMethod(accessName.substring(0, accessName.length() - 2)).invoke(instance)
                : c.getField(accessName).get(instance);
            return result == null ? null : expectedType.cast(result);
        } catch (Exception e) {
            if (returnNullOnFail) {
                return null;
            }
            throw new RuntimeException(e);
        }
    }

    private static Object access(Object instance, String accessName, Class<?> expectedType) {
        return access(instance, accessName, expectedType, false);
    }

    public static <T> T accessSequenced(
        Object instance,
        List<String> accessNames,
        Class<?> expectedType,
        boolean returnNullOnFail
    ) {
        try {
            for (String accessName : accessNames) {
                instance = access(instance, accessName, expectedType);
            }
            return instance == null ? null : (T) expectedType.cast(instance);
        } catch (Exception e) {
            if (returnNullOnFail) {
                return null;
            }
            throw new RuntimeException(e);
        }
    }

    public static <T> T accessSequenced(Object instance, List<String> accessNames, Class<?> expectedType) {
        return accessSequenced(instance, accessNames, expectedType, false);
    }
}
