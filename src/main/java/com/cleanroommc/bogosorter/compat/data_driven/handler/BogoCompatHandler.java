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
    JsonSchema<Class<? extends Container>> TARGET_SCHEMA = JsonSchema.STRING
        .map(DataDrivenUtils::toClass)
        .map(DataDrivenUtils.requireSubClassOf(Container.class))
        .describe("""
            class name, for example `net.minecraft.inventory.Container`""");
    JsonSchema<Integer> ROW_SIZE_SCHEMA = JsonSchema.INT
        .describe("""
            Mostly used for determining the button position.
            If the container shape is not rectangular, try to use the row size of the first row""");
    Map<String, JsonSchema<? extends BogoCompatHandler>> REGISTRY = new ConcurrentHashMap<>();
    JsonSchema<BogoCompatHandler> SCHEMA = JsonSchema.lazy(() -> {
        REGISTRY.put("generic", GenericHandler.SCHEMA);
        REGISTRY.put("remove", RemoveHandler.SCHEMA);
        REGISTRY.put("slot_range", RangedSlotHandler.SCHEMA);
        REGISTRY.put("slot_mapped", MappedSlotHandler.SCHEMA);
        REGISTRY.put("set_button_pos", SetPosHandler.SCHEMA);
        return JsonSchema.dispatch(REGISTRY);
    });

    void handle(IBogoSortAPI api);
}
