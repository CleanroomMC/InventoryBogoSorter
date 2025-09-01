package com.cleanroommc.bogosorter.compat.data_driven.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
public class DataDrivenUtils {

    public static Class<?> toClass(String className) {
        Class<?> c;
        try {
            c = Class.forName(className, false, DataDrivenUtils.class.getClassLoader());
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

    public static JsonElement deepCopy(JsonElement json) {
        if (json instanceof JsonObject obj) {
            var result = new JsonObject();
            for (var entry : obj.entrySet()) {
                result.add(entry.getKey(), entry.getValue());
            }
            return result;

        } else if (json instanceof JsonArray array) {
            var result = new JsonArray();
            for (var element : array) {
                result.add(element);
            }
            return result;
        }

        return json;
    }

    public static JsonObject mergeJson(JsonObject base, JsonObject preferred) {
        base = deepCopy(base).getAsJsonObject();

        for (var entry : preferred.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            var existed = base.get(key);
            if (existed == null) {
                base.add(key, value);
            } else {
                base.add(key, mergeJson(existed, value));
            }
        }

        return base;
    }

    public static JsonArray mergeJson(JsonArray base, JsonArray preferred) {
        base = deepCopy(base).getAsJsonArray();

        for (var element : preferred) {
            if (!base.contains(element)) {
                base.add(element);
            }
        }

        return base;
    }

    public static JsonElement mergeJson(JsonElement base, JsonElement preferred) {
        if (base instanceof JsonObject baseObj && preferred instanceof JsonObject preferredObj) {
            return mergeJson(baseObj, preferredObj);
        }

        if (base instanceof JsonArray baseArray && preferred instanceof JsonArray preferredArray) {
            return mergeJson(baseArray, preferredArray);
        }

        return preferred;
    }

    public static <T> Predicate<T> buildAllMatchFilter(Collection<? extends Predicate<T>> filters) {
        var iter = filters.iterator();
        switch (filters.size()) {
            case 0:
                return (slot) -> true;
            case 1:
                return iter.next();
            case 2:
                return iter.next().and(iter.next());
            case 3:
                Predicate<T> p1 = iter.next(), p2 = iter.next(), p3 = iter.next();
                return t -> p1.test(t) && p2.test(t) && p3.test(t);
        }
        var predicates = filters.toArray((Predicate<T>[]) new Predicate[0]);
        return t -> {
            for (var predicate : predicates) {
                if (!predicate.test(t)) {
                    return false;
                }
            }
            return true;
        };
    }

    public static <T> Predicate<T> buildAnyMatchFilter(Collection<? extends Predicate<T>> filters) {
        var iter = filters.iterator();
        switch (filters.size()) {
            case 0:
                return (slot) -> false;
            case 1:
                return iter.next();
            case 2:
                return iter.next().or(iter.next());
            case 3:
                Predicate<T> p1 = iter.next(), p2 = iter.next(), p3 = iter.next();
                return t -> p1.test(t) || p2.test(t) || p3.test(t);
        }
        var predicates = filters.toArray((Predicate<T>[]) new Predicate[0]);
        return t -> {
            for (var predicate : predicates) {
                if (predicate.test(t)) {
                    return true;
                }
            }
            return false;
        };
    }
}
