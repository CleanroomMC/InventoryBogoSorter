package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.utils.DataDrivenUtils;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import net.minecraft.inventory.Container;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZZZank
 */
public interface BogoCompatHandler {
    JsonSchema<Class<?>> CLASS_SCHEMA = JsonSchema.STRING.map(DataDrivenUtils::toClass);
    JsonSchema<Class<? extends Container>> TARGET_SCHEMA = CLASS_SCHEMA
        .<Class<? extends Container>>map(c -> DataDrivenUtils.requireSubClassOf(c, Container.class))
        .describe("""
            class name, for example `net.minecraft.inventory.Container`""");
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
}
