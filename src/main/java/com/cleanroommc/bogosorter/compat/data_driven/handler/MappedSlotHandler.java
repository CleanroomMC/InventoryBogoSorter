package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.compat.data_driven.condition.BogoCondition;
import com.cleanroommc.bogosorter.compat.data_driven.utils.DataDrivenUtils;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.ObjectJsonSchema;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
class MappedSlotHandler extends HandlerBase {
    public static final JsonSchema<MappedSlotHandler> SCHEMA = ObjectJsonSchema.of(
        BogoCondition.SCHEMA.toOptionalField("condition"),
        TARGET_SCHEMA.toField("target"),
        JsonSchema.INT.toField("rowSize"),
        MappedSlotActions.FILTER_SCHEMA.toList()
            .toOptionalField("slotFilters", Collections.emptyList()),
        MappedSlotActions.REDUCER_SCHEMA
            .toOptionalField("slotReducer", IBogoSortAPI.getInstance()::getSlot),
        MappedSlotHandler::new
    );

    private final int rowSize;
    private final Predicate<Slot> filter;
    private final Function<Slot, ISlot> reducer;

    public MappedSlotHandler(
        Optional<BogoCondition> condition,
        Class<? extends Container> target,
        int rowSize,
        List<Predicate<Slot>> filters,
        Function<Slot, ISlot> reducer
    ) {
        super(condition, target);
        this.rowSize = rowSize;
        this.filter = DataDrivenUtils.buildAllMatchFilter(filters);
        this.reducer = reducer;
        BogoSorter.LOGGER.info(
            "constructed mapped bogo compat handler targeting '{}' with row size '{}'",
            target.getName(),
            rowSize
        );
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
