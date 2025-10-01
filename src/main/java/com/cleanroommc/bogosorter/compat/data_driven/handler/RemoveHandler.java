package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.github.bsideup.jabel.Desugar;
import net.minecraft.inventory.Container;

/**
 * @author ZZZank
 */
@Desugar
record RemoveHandler(
    Class<? extends Container> target
) implements BogoCompatHandler {
    public static final JsonSchema<RemoveHandler> SCHEMA = JsonSchema.object(
        TARGET_SCHEMA.toField("target"),
        RemoveHandler::new
    ).describe("Remove sorting compat for the container");

    @Override
    public void handle(IBogoSortAPI api) {
        api.removeCompat(target);
    }
}
