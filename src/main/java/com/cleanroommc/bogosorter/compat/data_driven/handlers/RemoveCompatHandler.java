package com.cleanroommc.bogosorter.compat.data_driven.handlers;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;

/**
 * @author ZZZank
 */
public class RemoveCompatHandler extends CompatHandlerBase {
    public RemoveCompatHandler(String className) {
        super(className);
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.removeCompat(toClass());
    }
}
