package com.cleanroommc.bogosorter.compat.data_driven.handlers;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;

/**
 * @author ZZZank
 */
public class MarkOnlyCompatHandler extends CompatHandlerBase {
    public MarkOnlyCompatHandler(String className) {
        super(className);
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.addCompat(toClass(), (a, b) -> {});
    }
}
