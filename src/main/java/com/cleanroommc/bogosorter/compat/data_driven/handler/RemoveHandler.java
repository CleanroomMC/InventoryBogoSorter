package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;

/**
 * @author ZZZank
 */
public class RemoveHandler extends HandlerBase {
    public RemoveHandler(String className) {
        super(className);
        BogoSorter.LOGGER.info("constructed remove-only bogo compat handler targeting: '{}'", className);
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.removeCompat(toClass());
    }
}
