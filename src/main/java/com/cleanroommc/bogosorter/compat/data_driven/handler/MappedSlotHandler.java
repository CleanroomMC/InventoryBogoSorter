package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.compat.data_driven.BogoCompatParser;
import com.cleanroommc.bogosorter.compat.data_driven.condition.BogoCondition;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.ObjectJsonSchema;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
public class MappedSlotHandler extends HandlerBase {
    public static final JsonSchema<MappedSlotHandler> SCHEMA = ObjectJsonSchema.of(
        BogoCondition.SCHEMA.toOptionalField("condition"),
        TARGET_SCHEMA.toField("target"),
        JsonSchema.INT.toField("rowSize"),
        JsonSchema.JSON_OBJECT.map(BogoCompatParser::parseSingleMappedFilter).toList()
            .toOptionalField("slotFilters", List.of()),
        JsonSchema.JSON_OBJECT.map(BogoCompatParser::parseMappedReducer)
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
        this.filter = buildAllMatchFilter(filters);
        this.reducer = reducer;
        BogoSorter.LOGGER.info(
            "constructed mapped bogo compat handler targeting '{}' with row size '{}'",
            target.getName(),
            rowSize
        );
    }

    private static Predicate<Slot> buildAllMatchFilter(Collection<? extends Predicate<Slot>> filters) {
        var iter = filters.iterator();
        switch (filters.size()) {
            case 0:
                return (slot) -> true;
            case 1:
                return iter.next();
            case 2:
                return iter.next().and(iter.next());
            case 3:
                Predicate<Slot> pred1 = iter.next(), pred2 = iter.next(), pred3 = iter.next();
                return slot -> pred1.test(slot) && pred2.test(slot) && pred3.test(slot);
        }
        var predicates =(Predicate<Slot>[]) filters.toArray(new Predicate[0]);
        return slot -> {
            for (var predicate : predicates) {
                if (!predicate.test(slot)) {
                    return false;
                }
            }
            return true;
        };
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
