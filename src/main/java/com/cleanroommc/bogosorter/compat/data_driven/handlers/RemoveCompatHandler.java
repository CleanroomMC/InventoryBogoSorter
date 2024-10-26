package com.cleanroommc.bogosorter.compat.data_driven.handlers;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;

/**
 * @author ZZZank
 */
public class RemoveCompatHandler extends CompatHandlerBase {
    public RemoveCompatHandler(String className) {
        super(className);
        BogoSorter.LOGGER.info("constructed remove-only bogo compat handler targeting: '{}'", className);
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.removeCompat(toClass());
    }
}
