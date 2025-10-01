package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.github.bsideup.jabel.Desugar;
import net.minecraft.inventory.Container;

/**
 * @author ZZZank
 */
@Desugar
record MarkOnlyHandler(
    Class<? extends Container> target
) implements BogoCompatHandler {
    public static final JsonSchema<MarkOnlyHandler> SCHEMA = JsonSchema.object(
        TARGET_SCHEMA.toField("target"),
        MarkOnlyHandler::new
    ).describe("Marks the container, but do nothing");

    @Override
    public void handle(IBogoSortAPI api) {
        api.addCompat(target, (a, b) -> {});
    }
}
