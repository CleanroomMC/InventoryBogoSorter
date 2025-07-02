package com.cleanroommc.bogosorter.compat.data_driven.utils;

import java.util.Objects;

/**
 * @author ZZZank
 */
public interface UnsafeMapper<I, O> {
    static <I, T, O> UnsafeMapper<I, O> chain(UnsafeMapper<I, T> first, UnsafeMapper<T, O> second) {
        return input -> second.map(first.map(input));
    }

    static <I, T1, T2, O> UnsafeMapper<I, O> chain(
        UnsafeMapper<I, T1> first,
        UnsafeMapper<T1, T2> second,
        UnsafeMapper<T2, O> third
    ) {
        return input -> third.map(second.map(first.map(input)));
    }

    static <I, O> Memorizing<I, O> memorize(UnsafeMapper<I, O> original) {
        return new Memorizing<>(original);
    }

    O map(I o) throws Exception;

    class Memorizing<I, O> implements UnsafeMapper<I, O> {
        private final UnsafeMapper<I, O> wrapped;
        private O returnCache;
        private Object inputCache;
        private boolean cached;

        public Memorizing(UnsafeMapper<I, O> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public O map(I o) throws Exception {
            if (cached && Objects.equals(inputCache, o)) {
                return returnCache;
            }
            cached = true;
            inputCache = o;
            returnCache = wrapped.map(o);
            return returnCache;
        }
    }
}
