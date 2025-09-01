package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.compat.FixedLimitSlot;
import com.cleanroommc.bogosorter.compat.data_driven.utils.ReflectUtils;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.ObjectJsonSchema;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.PrimitiveJsonSchema;
import net.minecraft.inventory.Slot;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
class MappedSlotActions {
    public static final Map<String, JsonSchema<Function<Slot, ISlot>>> REDUCER_REGISTRY = new LinkedHashMap<>();
    public static final JsonSchema<Function<Slot, ISlot>> REDUCER_SCHEMA = JsonSchema.lazy(() -> {
        REDUCER_REGISTRY.put("general", new PrimitiveJsonSchema<>(
            ignored -> IBogoSortAPI.getInstance()::getSlot,
            "object"
        ));
        REDUCER_REGISTRY.put("custom_stack_limit", ObjectJsonSchema.of(
            JsonSchema.INT.toField("limit"),
            limit -> slot -> new FixedLimitSlot(slot, limit)
        ));
        return JsonSchema.dispatch(REDUCER_REGISTRY);
    });

    public static final Map<String, JsonSchema<Predicate<Slot>>> FILTER_REGISTRY = new LinkedHashMap<>();
    public static final JsonSchema<Predicate<Slot>> FILTER_SCHEMA = JsonSchema.dispatch(FILTER_REGISTRY)
        .extractToDefinitions("mapped_slot_filter");
    public static final ObjectJsonSchema<Predicate<Slot>> FILTER_SCHEMA_INSTANCEOF = ObjectJsonSchema.of(
        BogoCompatHandler.CLASS_SCHEMA
            .<Class<? extends Slot>>map(c -> ReflectUtils.requireSubClassOf(c, Slot.class))
            .toField("class"),
        c -> c::isInstance
    );
    public static final ObjectJsonSchema<Predicate<Slot>> FILTER_SCHEMA_RANGED = ObjectJsonSchema.of(
        JsonSchema.INT.toField("min"),
        JsonSchema.INT.toField("max"),
        (min, max) -> (slot) -> {
            var slotIndex = slot.getSlotIndex();
            return slotIndex >= min && slotIndex <= max;
        }
    );
    public static final ObjectJsonSchema<Predicate<Slot>> FILTER_SCHEMA_AND = ObjectJsonSchema.of(
        FILTER_SCHEMA.toList().toField("filters"),
        filters -> slot -> filters.stream().allMatch(p -> p.test(slot))
    );
    public static final ObjectJsonSchema<Predicate<Slot>> FILTER_SCHEMA_OR = ObjectJsonSchema.of(
        FILTER_SCHEMA.toList().toField("filters"),
        filters -> slot -> filters.stream().anyMatch(p -> p.test(slot))
    );

    static {
        FILTER_REGISTRY.put("instanceof", FILTER_SCHEMA_INSTANCEOF);
        FILTER_REGISTRY.put("index_in_range", FILTER_SCHEMA_RANGED);
        FILTER_REGISTRY.put("and", FILTER_SCHEMA_AND);
        FILTER_REGISTRY.put("or", FILTER_SCHEMA_OR);
    }
}
