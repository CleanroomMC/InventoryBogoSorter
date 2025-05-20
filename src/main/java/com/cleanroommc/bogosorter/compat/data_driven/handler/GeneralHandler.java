package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZZZank
 */
public class GeneralHandler extends HandlerBase {

    public GeneralHandler(@NotNull JsonObject o) {
        super(readClass(o));
        BogoSorter.LOGGER.info("constructed general bogo compat handler targeting: '{}'", target.getName());
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.addCompat(target, (container, builder) -> builder.addGenericSlotGroup());
    }
}
