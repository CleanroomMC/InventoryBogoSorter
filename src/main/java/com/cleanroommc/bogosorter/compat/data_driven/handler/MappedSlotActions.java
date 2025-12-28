package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.compat.FixedLimitSlot;
import com.cleanroommc.bogosorter.compat.data_driven.utils.DataDrivenUtils;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
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
    public static final JsonSchema<Function<Slot, ISlot>> REDUCER_SCHEMA = JsonSchema.dispatch(REDUCER_REGISTRY)
        .describe("Convert slot definition to the one recognizable by BogoSorter")
        .extractToDefinitions("mapped_slot_reducer");
    public static final Function<Slot, ISlot> DEFAULT_SLOT_REDUCER = IBogoSortAPI.getInstance()::getSlot;

    static {
        REDUCER_REGISTRY.put("general", new PrimitiveJsonSchema<>(
            ignored -> DEFAULT_SLOT_REDUCER,
            "object"
        ).describe("Default slot representation used by BogoSorter"));
        REDUCER_REGISTRY.put("custom_stack_limit", JsonSchema.object(
            JsonSchema.INT.toField("limit"),
            limit -> (Function<Slot, ISlot>)slot -> new FixedLimitSlot(slot, limit)
        ).describe("Slot with customizable stack limit"));
    }

    public static final Map<String, JsonSchema<Predicate<Slot>>> FILTER_REGISTRY = new LinkedHashMap<>();
    public static final JsonSchema<Predicate<Slot>> FILTER_SCHEMA = JsonSchema.dispatch(FILTER_REGISTRY)
        .describe("Only accepted slots will be added to slot group")
        .extractToDefinitions("mapped_slot_filter");

    public static final JsonSchema<Predicate<Slot>> FILTER_SCHEMA_INSTANCEOF = JsonSchema.object(
        JsonSchema.STRING
            .map(DataDrivenUtils::toClass)
            .map(DataDrivenUtils.requireSubClassOf(Slot.class))
            .describe("Class name, for example `net.minecraft.inventory.Slot`")
            .toField("class"),
        c -> (Predicate<Slot>) c::isInstance
    ).describe("Accept slots that are instance of the `class`");
    public static final JsonSchema<Predicate<Slot>> FILTER_SCHEMA_RANGED = JsonSchema.object(
        JsonSchema.INT.describe("Index of the first slot (including)").toField("start"),
        JsonSchema.INT.describe("Index of the end slot (excluding)").toField("end"),
        (min, max) -> (Predicate<Slot>) (slot) -> {
            var slotIndex = slot.getSlotIndex();
            return slotIndex >= min && slotIndex <= max;
        }
    ).describe("Accept slots with index in [start, end) range");
    public static final JsonSchema<Predicate<Slot>> FILTER_SCHEMA_AND = JsonSchema.object(
        FILTER_SCHEMA.toList().toField("filters"),
        DataDrivenUtils::buildAllMatchFilter
    ).describe("Accept slots accepted by all sub filters");
    public static final JsonSchema<Predicate<Slot>> FILTER_SCHEMA_OR = JsonSchema.object(
        FILTER_SCHEMA.toList().toField("filters"),
        DataDrivenUtils::buildAnyMatchFilter
    ).describe("Accept slots accepted by any of the sub filters");
    public static final JsonSchema<Predicate<Slot>> FILTER_SCHEMA_NOT = JsonSchema.object(
        FILTER_SCHEMA.toField("filter"),
        Predicate::negate
    ).describe("Accept slots denied by the sub filter");

    static {
        FILTER_REGISTRY.put("instanceof", FILTER_SCHEMA_INSTANCEOF);
        FILTER_REGISTRY.put("index_in_range", FILTER_SCHEMA_RANGED);
        FILTER_REGISTRY.put("and", FILTER_SCHEMA_AND);
        FILTER_REGISTRY.put("or", FILTER_SCHEMA_OR);
        FILTER_REGISTRY.put("not", FILTER_SCHEMA_NOT);
    }
}
