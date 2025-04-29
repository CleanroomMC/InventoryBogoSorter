package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;

/**
 * @author ZZZank
 */
public class MarkOnlyHandler extends HandlerBase {
    public MarkOnlyHandler(String className) {
        super(className);
        BogoSorter.LOGGER.info("constructed mark-only bogo compat handler targeting: '{}'", className);
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.addCompat(toClass(), (a, b) -> {});
    }
}
