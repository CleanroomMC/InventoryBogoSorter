package com.cleanroommc.bogosorter.compat.data_driven;

import net.minecraftforge.fml.common.Loader;

import java.util.List;

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

    static BogoCondition and(List<BogoCondition> conditions) {
        return switch (conditions.size()) {
            case 0 -> ALWAYS;
            case 1 -> conditions.get(0);
            case 2 -> {
                var cond1 = conditions.get(0);
                var cond2 = conditions.get(1);
                yield () -> cond1.test() && cond2.test();
            }
            default -> () -> conditions.stream().allMatch(BogoCondition::test);
        };
    }

    static BogoCondition or(List<BogoCondition> conditions) {
        return switch (conditions.size()) {
            case 0 -> NEVER;
            case 1 -> conditions.get(0);
            case 2 -> {
                var cond1 = conditions.get(0);
                var cond2 = conditions.get(1);
                yield () -> cond1.test() || cond2.test();
            }
            default -> () -> conditions.stream().anyMatch(BogoCondition::test);
        };
    }

    boolean test();
}
