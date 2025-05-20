package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlot;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
public class MappedSlotHandler extends HandlerBase {
    private final int rowSize;
    private final Predicate<Slot> filter;
    private final Function<Slot, ISlot> reducer;

    public MappedSlotHandler(
        Class<? extends Container> target,
        int rowSize,
        List<Predicate<Slot>> filters,
        Function<Slot, ISlot> reducer
    ) {
        super(target);
        this.rowSize = rowSize;
        this.filter = andCompressed(filters);
        this.reducer = reducer;
        BogoSorter.LOGGER.info(
            "constructed mapped bogo compat handler targeting '{}' with row size '{}'",
            target.getName(),
            rowSize
        );
    }

    private static Predicate<Slot> andCompressed(Collection<? extends Predicate<Slot>> filters) {
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
