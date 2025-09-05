package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.compat.data_driven.condition.BogoCondition;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
class MappedSlotHandler extends HandlerBase {
    public static final JsonSchema<MappedSlotHandler> SCHEMA = JsonSchema.object(
        CONDITION_SCHEMA.toOptionalField("condition"),
        TARGET_SCHEMA.toField("target"),
        ROW_SIZE_SCHEMA.toField("rowSize"),
        MappedSlotActions.FILTER_SCHEMA
            .toOptionalField("slot_filter", slot -> true),
        MappedSlotActions.REDUCER_SCHEMA
            .toOptionalField("slot_reducer", MappedSlotActions.DEFAULT_SLOT_REDUCER),
        MappedSlotHandler::new
    ).describe("Registry slot group with optional custom slot filtering and slot converter");

    private final int rowSize;
    private final Predicate<Slot> filter;
    private final Function<Slot, ISlot> reducer;

    public MappedSlotHandler(
        Optional<BogoCondition> condition,
        Class<? extends Container> target,
        int rowSize,
        Predicate<Slot> filter,
        Function<Slot, ISlot> reducer
    ) {
        super(condition, target);
        this.rowSize = rowSize;
        this.filter = filter;
        this.reducer = reducer;
    }

    @Override
    protected void handleImpl(IBogoSortAPI api) {
        api.addCompat(
            target(),
            (container, builder) -> {
                var slots = new ArrayList<ISlot>();
                for (var slot : container.inventorySlots) {
                    if (filter.test(slot)) {
                        slots.add(reducer.apply(slot));
                    }
                }
                builder.addSlotGroup(slots, rowSize);
            }
        );
    }
}
