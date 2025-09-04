package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.condition.BogoCondition;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import net.minecraft.inventory.Container;

import java.util.Optional;

/**
 * @author ZZZank
 */
class RemoveHandler extends HandlerBase {
    public static final JsonSchema<RemoveHandler> SCHEMA = JsonSchema.object(
        CONDITION_SCHEMA.toOptionalField("condition"),
        TARGET_SCHEMA.toField("target"),
        RemoveHandler::new
    ).describe("Remove sorting compat for the container");

    protected RemoveHandler(
        Optional<BogoCondition> condition,
        Class<? extends Container> target
    ) {
        super(condition, target);
    }

    @Override
    protected void handleImpl(IBogoSortAPI api) {
        api.removeCompat(target);
    }
}
