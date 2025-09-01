package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.utils.ReflectUtils;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.google.gson.Gson;
import net.minecraft.inventory.Container;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZZZank
 */
public interface BogoCompatHandler {
    JsonSchema<Class<?>> CLASS_SCHEMA = JsonSchema.STRING.map(ReflectUtils::toClass);
    JsonSchema<Class<? extends Container>> TARGET_SCHEMA = CLASS_SCHEMA
        .describe("class name, for example `net.minecraft.inventory.Container`")
        .map(c -> ReflectUtils.requireSubClassOf(c, Container.class));
    Map<String, JsonSchema<? extends BogoCompatHandler>> REGISTRY = new ConcurrentHashMap<>();
    JsonSchema<BogoCompatHandler> SCHEMA = JsonSchema.lazy(() -> {
        REGISTRY.put("generic", GenericHandler.SCHEMA);
        REGISTRY.put("remove", RemoveHandler.SCHEMA);
        REGISTRY.put("mark_only", MarkOnlyHandler.SCHEMA);
        REGISTRY.put("slot_range", RangedSlotHandler.SCHEMA);
        REGISTRY.put("slot_mapped", MappedSlotHandler.SCHEMA);
        REGISTRY.put("set_button_pos", SetPosHandler.SCHEMA);
        return JsonSchema.dispatch(REGISTRY);
    });

    void handle(IBogoSortAPI api);

    static void main(String[] args) throws IOException {
        var schema = SCHEMA.toList().getSchema();
        System.out.println(new Gson().toJson(schema));
    }
}
