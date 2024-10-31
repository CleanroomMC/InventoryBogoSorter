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

    /**
     * this will not check whether the mod specified by {@code modid} is present, make sure
     * {@link #modloaded(String)} is true for the {@code modid} before using returned {@link BogoCondition}
     * @param versionPattern RegEx string
     */
    static BogoCondition modVersionMatched(String modid, String versionPattern) {
        return () -> Loader.instance()
            .getIndexedModList()
            .get(modid)
            .getVersion()
            .matches(versionPattern);
    }

    static BogoCondition not(BogoCondition condition) {
        return () -> !condition.test();
    }

    static BogoCondition and(BogoCondition... conditions) {
        return switch (conditions.length) {
            case 0 -> ALWAYS;
            case 1 -> conditions[0];
            case 2 -> () -> conditions[0].test() && conditions[1].test();
            default -> () -> Arrays.stream(conditions).allMatch(BogoCondition::test);
        };
    }

    static BogoCondition or(BogoCondition... conditions) {
        return switch (conditions.length) {
            case 0 -> NEVER;
            case 1 -> conditions[0];
            case 2 -> () -> conditions[0].test() || conditions[1].test();
            default -> () -> Arrays.stream(conditions).anyMatch(BogoCondition::test);
        };
    }

    boolean test();
}
