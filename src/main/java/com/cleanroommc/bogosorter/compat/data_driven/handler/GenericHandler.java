package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.github.bsideup.jabel.Desugar;
import net.minecraft.inventory.Container;

/**
 * @author ZZZank
 */
@Desugar
record GenericHandler(
    Class<? extends Container> target
) implements BogoCompatHandler {
    public static final JsonSchema<GenericHandler> SCHEMA = JsonSchema.object(
        TARGET_SCHEMA.toField("target"),
        GenericHandler::new
    ).describe("""
        Creates and registers a generic slot group.
        It assumes that all non player slots belong to the same group and that the slot group has a rectangular shape.""");

    @Override
    public void handle(IBogoSortAPI api) {
        api.addGenericCompat(target);
    }
}
