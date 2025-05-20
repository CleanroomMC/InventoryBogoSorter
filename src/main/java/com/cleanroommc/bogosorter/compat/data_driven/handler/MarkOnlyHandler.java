package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZZZank
 */
public class MarkOnlyHandler extends HandlerBase {
    public MarkOnlyHandler(@NotNull JsonObject o) {
        super(readClass(o));
        BogoSorter.LOGGER.info("constructed mark-only bogo compat handler targeting: '{}'", target);
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.addCompat(target, (a, b) -> {});
    }
}
