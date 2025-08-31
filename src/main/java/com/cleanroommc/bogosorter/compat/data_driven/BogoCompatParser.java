package com.cleanroommc.bogosorter.compat.data_driven;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.compat.FixedLimitSlot;
import com.cleanroommc.bogosorter.compat.data_driven.utils.DataDrivenReflection;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
public class BogoCompatParser {

    /// ```
    /// {
    ///     type: "general"
    /// }
    /// or
    /// {
    ///     type: "custom_stack_limit",
    ///     limit: int
    /// }
    /// ```
    public static Function<Slot, ISlot> parseMappedReducer(@NotNull JsonObject o) {
        return switch (o.get("type").getAsString()) {
            case "general" -> IBogoSortAPI.getInstance()::getSlot;
            case "custom_stack_limit" -> {
                int limit = o.get("limit").getAsInt();
                yield slot -> new FixedLimitSlot(slot, limit);
            }
            default -> throw new IllegalStateException("Unexpected value: " + o.get("type"));
        };
    }

    /// filter sequence, with each element matching:
    /// ```
    /// {
    ///     type: "instanceof",
    ///     class: "clazz.path.Here"
    /// }
    /// ```
    /// or
    /// ```
    /// {
    ///     type: "index_in_range",
    ///     min: int,
    ///     max: int
    /// }
    /// ```
    /// or
    /// ```
    /// {
    ///     type: "or",
    ///     filters: filter[]
    /// }
    /// ```
    public static List<Predicate<Slot>> parseMappedFilter(@NotNull JsonArray filterJsons) {
        var compiled = new ArrayList<Predicate<Slot>>();
        for (var filterJson : filterJsons) {
            var obj = filterJson.getAsJsonObject();
            Predicate<Slot> filter = parseSingleMappedFilter(obj);
            compiled.add(filter);
        }
        return compiled;
    }

    public static @NotNull Predicate<Slot> parseSingleMappedFilter(JsonObject obj) {
        return switch (obj.get("type").getAsString()) {
            case "instanceof" -> DataDrivenReflection.toClass(
                obj.get("class"),
                Slot.class
            )::isInstance;
            case "index_in_range" -> {
                int min = obj.get("min").getAsInt();
                int max = obj.get("max").getAsInt();
                yield (slot) -> slot.getSlotIndex() >= min && slot.getSlotIndex() <= max;
            }
            case "or" -> {
                var filters = parseMappedFilter(obj.get("filters").getAsJsonArray());
                yield slot -> filters.stream().anyMatch(p -> p.test(slot));
            }
            default -> throw new IllegalStateException("Unexpected type: " + obj.get("type"));
        };
    }
}
