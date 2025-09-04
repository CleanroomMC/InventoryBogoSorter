package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.condition.BogoCondition;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import net.minecraft.inventory.Container;

import java.util.Optional;

/**
 * @author ZZZank
 */
class GenericHandler extends HandlerBase {
    public static final JsonSchema<GenericHandler> SCHEMA = JsonSchema.object(
        CONDITION_SCHEMA.toOptionalField("condition"),
        TARGET_SCHEMA.toField("target"),
        GenericHandler::new
    ).describe("""
        Creates and registers a generic slot group.
        It assumes that all non player slots belong to the same group and that the slot group has a rectangular shape.""");

    protected GenericHandler(Optional<BogoCondition> condition, Class<? extends Container> target) {
        super(condition, target);
    }

    @Override
    protected void handleImpl(IBogoSortAPI api) {
        api.addCompat(target(), (container, builder) -> builder.addGenericSlotGroup());
    }
}
