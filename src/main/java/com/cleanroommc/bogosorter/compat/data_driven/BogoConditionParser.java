package com.cleanroommc.bogosorter.compat.data_driven;

import com.cleanroommc.bogosorter.BogoSorter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZZank
 */
public class BogoConditionParser {
    @NotNull
    public static BogoCondition parse(@NotNull JsonObject o) {
        var conditions = new ArrayList<BogoCondition>();
        for (var entry : o.entrySet()) {
            var value = entry.getValue();
            var condition = switch (entry.getKey()) {
                case "mod" -> parseModCondition(entry.getValue());
                case "not" -> BogoCondition.not(parse(value.getAsJsonObject()));
                case "and" -> BogoCondition.and(parseList(value.getAsJsonArray()));
                case "or" -> BogoCondition.or(parseList(value.getAsJsonArray()));
                default -> null;
            };
            if (condition != null) {
                conditions.add(condition);
            }
        }
        if (conditions.isEmpty()) {
            BogoSorter.LOGGER.warn("no valid logics in: {}", o);
            return BogoCondition.ALWAYS;
        }
        return BogoCondition.and(conditions);
    }

    public static @NotNull ArrayList<BogoCondition> parseList(JsonArray value) {
        ArrayList<BogoCondition> parsed = new ArrayList<>();
        for (JsonElement element : value) {
            parsed.add(parse(element.getAsJsonObject()));
        }
        return parsed;
    }

    public static BogoCondition parseModCondition(JsonElement element) {
        if (element.isJsonPrimitive()) {
            return BogoCondition.modloaded(element.getAsString());
        }

        var o = element.getAsJsonObject();
        List<BogoCondition> conditions = new ArrayList<>();

        var id = o.get("id").getAsString();
        conditions.add(BogoCondition.modloaded(id));

        var versionPattern = o.get("version_pattern");
        if (versionPattern != null) {
            conditions.add(BogoCondition.modVersionMatched(id, versionPattern.getAsString()));
        }

        return BogoCondition.and(conditions);
    }
}
