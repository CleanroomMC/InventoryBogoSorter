package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.github.bsideup.jabel.Desugar;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
@Desugar
record MappedSlotHandler(
    Class<? extends Container> target,
    int rowSize,
    Predicate<Slot> filter,
    Function<Slot, ISlot> reducer
) implements BogoCompatHandler {
    public static final JsonSchema<MappedSlotHandler> SCHEMA = JsonSchema.object(
        TARGET_SCHEMA.toField("target"),
        ROW_SIZE_SCHEMA.toField("row_size"),
        MappedSlotActions.FILTER_SCHEMA
            .toOptionalField("slot_filter", slot -> true),
        MappedSlotActions.REDUCER_SCHEMA
            .toOptionalField("slot_reducer", MappedSlotActions.DEFAULT_SLOT_REDUCER),
        MappedSlotHandler::new
    ).describe("Registry slot group with optional custom slot filtering and slot converter");

    @Override
    public void handle(IBogoSortAPI api) {
        api.addCompat(
            target,
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
