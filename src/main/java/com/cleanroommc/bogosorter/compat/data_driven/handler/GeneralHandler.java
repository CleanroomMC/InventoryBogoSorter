package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;

/**
 * @author ZZZank
 */
public class GeneralHandler extends HandlerBase {

    public GeneralHandler(String className) {
        super(className);
        BogoSorter.LOGGER.info("constructed general bogo compat handler targeting: '{}'", className);
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.addCompat(toClass(), (container, builder) -> additionalAction(builder.addGenericSlotGroup()));
    }
}
