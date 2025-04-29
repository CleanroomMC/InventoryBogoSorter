package com.cleanroommc.bogosorter.compat.data_driven;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.compat.FixedLimitSlot;
import com.cleanroommc.bogosorter.compat.data_driven.condition.BogoCondition;
import com.cleanroommc.bogosorter.compat.data_driven.handlers.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class BogoCompatParser {

    /// ```
    /// {
    ///     condition: {
    ///         ...
    ///     },
    ///     target: "clazz.path.Here",
    ///     type: "...",
    /// }
    /// ```
    @NotNull
    public static BogoCompatHandler parse(@NotNull JsonObject o) {
        var handler = parseHandler(o);

        var conditionJson = o.get("condition");
        if (conditionJson == null) {
            return handler;
        }
        var condition = BogoCondition.read(o.get("condition").getAsJsonObject());

        return api -> {
            if (condition.test()) {
                handler.handle(api);
            }
        };
    }

    public static @NotNull BogoCompatHandler parseHandler(@NotNull JsonObject o) {
        var name = o.get("target").getAsString();
        return switch (o.get("type").getAsString()) {
            case "general" -> new GeneralCompatHandler(name);
            case "remove" -> new RemoveCompatHandler(name);
            case "mark_only" -> new MarkOnlyCompatHandler(name);
            case "slot_range" -> parseRanged(o, name);
            case "slot_mapped" -> parseMapped(o, name);
            default -> throw new IllegalArgumentException();
        };
    }

    /// ```
    /// {
    ///     "type": "ranged",
    ///     "start": int,
    ///     "end": int,
    ///     "row_size": int
    /// }
    /// ```
    static RangedSlotCompatHandler parseRanged(@NotNull JsonObject o, String targetClassName) {
        int start = o.get("start").getAsNumber().intValue();
        int end = o.get("end").getAsNumber().intValue();
        int rowSize = o.get("row_size").getAsNumber().intValue();
        return new RangedSlotCompatHandler(targetClassName, start, end, rowSize);
    }

    /// ```
    /// {
    ///     row_size: int,
    ///     filters?: array,
    ///     mapper?: object
    /// }
    /// ```
    /// @see #parseMappedFilter(JsonArray)
    /// @see #parseMappedReducer(JsonObject)
    static MappedSlotCompatHandler parseMapped(@NotNull JsonObject o, String target) {
        int rowSize = o.get("row_size").getAsNumber().intValue();

        var filters = Optional.ofNullable(o.get("filters"))
            .map(JsonElement::getAsJsonArray)
            .map(BogoCompatParser::parseMappedFilter)
            .orElse(Collections.emptyList());

        var reducer = Optional.ofNullable(o.get("mapper"))
            .map(JsonElement::getAsJsonObject)
            .map(BogoCompatParser::parseMappedReducer)
            .orElseGet(() -> IBogoSortAPI.getInstance()::getSlot);

        return new MappedSlotCompatHandler(target, rowSize, slots -> {
            Stream<Slot> s = slots.stream();
            for (var filter : filters) {
                s = s.filter(filter);
            }
            return s.map(reducer).collect(Collectors.toList());
        });
    }

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
    static Function<Slot, ISlot> parseMappedReducer(@NotNull JsonObject o) {
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
    static List<Predicate<Slot>> parseMappedFilter(@NotNull JsonArray filterJsons) {
        var compiled = new ArrayList<Predicate<Slot>>();
        for (var filterJson : filterJsons) {
            var obj = filterJson.getAsJsonObject();
            Predicate<Slot> filter = switch (obj.get("type").getAsString()) {
                case "instanceof" -> CompatHandlerBase.toClass(
                    obj.get("class").getAsString(),
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
            compiled.add(filter);
        }
        return compiled;
    }
}
