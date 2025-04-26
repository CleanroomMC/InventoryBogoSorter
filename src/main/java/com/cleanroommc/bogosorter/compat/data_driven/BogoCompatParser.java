package com.cleanroommc.bogosorter.compat.data_driven;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.compat.FixedLimitSlot;
import com.cleanroommc.bogosorter.compat.data_driven.handlers.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class BogoCompatParser {

    @NotNull
    public static BogoCompatHandler parse(@NotNull JsonObject o) {
        JsonElement condition = o.get("condition");
        if (condition == null) {
            return parseHandler(o);
        }
        return api -> {
            if (BogoConditionParser.parse(condition.getAsJsonObject()).test()) {
                parseHandler(o).handle(api);
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

    /**
     * {@code {
     *     "type": "...",
     *     "start": 0,
     *     "end": 1,
     *     "row_size": 2
     * }}
     */
    static RangedSlotCompatHandler parseRanged(@NotNull JsonObject o, String targetClassName) {
        int start = o.get("start").getAsNumber().intValue();
        int end = o.get("end").getAsNumber().intValue();
        int rowSize = o.get("row_size").getAsNumber().intValue();
        return new RangedSlotCompatHandler(targetClassName, start, end, rowSize);
    }

    static MappedSlotCompatHandler parseMapped(@NotNull JsonObject o, String target) {
        int rowSize = o.get("row_size").getAsNumber().intValue();
        List<Predicate<Slot>> filters = o.has("filters")
            ? parseMappedFilter(o.get("filters").getAsJsonArray())
            : Collections.emptyList();
        Function<Slot, ISlot> reducer = o.has("mapper")
            ? parseMappedReducer(o.get("mapper").getAsJsonObject())
            : IBogoSortAPI.getInstance()::getSlot;
        return new MappedSlotCompatHandler(target, rowSize, slots -> {
            Stream<Slot> s = slots.stream();
            for (var filter : filters) {
                s = s.filter(filter);
            }
            return s.map(reducer).collect(Collectors.toList());
        });
    }

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

    static List<Predicate<Slot>> parseMappedFilter(@NotNull JsonArray filters) {
        var compiled = new ArrayList<Predicate<Slot>>();
        for (JsonElement filter : filters) {
            var parts = filter.getAsJsonObject().entrySet().iterator().next();
            Predicate<Slot> compiledAction = switch (parts.getKey()) {
                case "instanceof" -> {
                    Class<? extends Slot> typeFilter = CompatHandlerBase.toClass(parts.getValue().getAsString(), Slot.class);
                    yield (slot) -> typeFilter.isAssignableFrom(slot.getClass());
                }
                case "index_in_range" -> {
                    JsonObject range = parts.getValue().getAsJsonObject();
                    int min = range.get("min").getAsInt();
                    int max = range.get("max").getAsInt();
                    yield (slot) -> slot.getSlotIndex() >= min && slot.getSlotIndex() <= max;
                }
                case "or" -> slot -> parseMappedFilter(parts.getValue().getAsJsonArray()).stream().anyMatch(p -> p.test(slot));
                default -> throw new IllegalStateException("Unexpected value: " + parts.getKey());
            };
            compiled.add(compiledAction);
        }
        return compiled;
    }
}
