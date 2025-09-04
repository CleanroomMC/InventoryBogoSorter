package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.condition.BogoCondition;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import net.minecraft.inventory.Container;

import java.util.Optional;

/**
 * @author ZZZank
 */
class MarkOnlyHandler extends HandlerBase {
    public static final JsonSchema<MarkOnlyHandler> SCHEMA = JsonSchema.object(
        CONDITION_SCHEMA.toOptionalField("condition"),
        TARGET_SCHEMA.toField("target"),
        MarkOnlyHandler::new
    ).describe("Marks the container, but do nothing");

    protected MarkOnlyHandler(Optional<BogoCondition> condition, Class<? extends Container> target) {
        super(condition, target);
    }

    @Override
    protected void handleImpl(IBogoSortAPI api) {
        api.addCompat(target, (a, b) -> {});
    }
}
