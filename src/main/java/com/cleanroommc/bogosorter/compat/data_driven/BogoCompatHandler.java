package com.cleanroommc.bogosorter.compat.data_driven;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.handler.*;
import com.cleanroommc.bogosorter.compat.data_driven.utils.DataDrivenReflection;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.DispatchJsonSchema;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import net.minecraft.inventory.Container;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZZZank
 */
public interface BogoCompatHandler {
    JsonSchema<Class<? extends Container>> TARGET_SCHEMA = JsonSchema.STRING
        .map(name -> DataDrivenReflection.toClass(name, Container.class));
    Map<String, JsonSchema<? extends BogoCompatHandler>> REGISTRY = new ConcurrentHashMap<>();
    JsonSchema<BogoCompatHandler> SCHEMA = JsonSchema.lazy(() -> {
        REGISTRY.put("generic", GenericHandler.SCHEMA);
        REGISTRY.put("remove", RemoveHandler.SCHEMA);
        REGISTRY.put("mark_only", MarkOnlyHandler.SCHEMA);
        REGISTRY.put("slot_range", RangedSlotHandler.SCHEMA);
        REGISTRY.put("slot_mapped", MappedSlotHandler.SCHEMA);
        REGISTRY.put("set_button_pos", SetPosHandler.SCHEMA);
        return new DispatchJsonSchema<>(REGISTRY, "type", null);
    });

    void handle(IBogoSortAPI api);
}
