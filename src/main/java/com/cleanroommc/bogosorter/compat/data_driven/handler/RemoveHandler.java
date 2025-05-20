package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZZZank
 */
public class RemoveHandler extends HandlerBase {
    public RemoveHandler(@NotNull JsonObject o) {
        super(readClass(o));
        BogoSorter.LOGGER.info("constructed remove-only bogo compat handler targeting: '{}'", target.getName());
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.removeCompat(target);
    }
}
