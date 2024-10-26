package com.cleanroommc.bogosorter.compat.data_driven.handlers;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;

/**
 * @author ZZZank
 */
public class GeneralCompatHandler extends CompatHandlerBase {

    public GeneralCompatHandler(String className) {
        super(className);
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.addGenericCompat(toClass());
    }
}
