package com.cleanroommc.bogosorter.compat.data_driven;

import net.minecraftforge.fml.common.Loader;

import java.util.Arrays;

/**
 * @author ZZZank
 */
public interface BogoCondition {
    BogoCondition ALWAYS = () -> true;
    BogoCondition NEVER = () -> false;

    static BogoCondition modloaded(String modid) {
        return () -> Loader.isModLoaded(modid);
    }

    static BogoCondition not(BogoCondition condition) {
        return () -> !condition.test();
    }

    static BogoCondition and(BogoCondition... conditions) {
        return conditions.length == 0
            ? ALWAYS :
            () -> Arrays.stream(conditions).allMatch(BogoCondition::test);
    }

    static BogoCondition or(BogoCondition... conditions) {
        return () -> Arrays.stream(conditions).anyMatch(BogoCondition::test);
    }

    boolean test();
}
