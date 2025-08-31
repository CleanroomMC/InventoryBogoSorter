package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.compat.data_driven.condition.BogoCondition;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.ObjectJsonSchema;
import net.minecraft.inventory.Container;

import java.util.Locale;
import java.util.Optional;

/**
 * @author ZZZank
 */
public class SetPosHandler extends HandlerBase {
    public static final JsonSchema<SetPosHandler> SCHEMA = ObjectJsonSchema.of(
        BogoCondition.SCHEMA.toOptionalField("condition"),
        TARGET_SCHEMA.toField("target"),
        JsonSchema.STRING.map(SetPosHandler::readPosSetter).toField("pos"),
        SetPosHandler::new
    );

    private final IPosSetter posSetter;

    protected SetPosHandler(
        Optional<BogoCondition> condition,
        Class<? extends Container> target,
        IPosSetter posSetter
    ) {
        super(condition, target);
        this.posSetter = posSetter;
    }

    private static IPosSetter readPosSetter(String str) {
        return switch (str.toUpperCase(Locale.ROOT)) {
            case "TOP_RIGHT_HORIZONTAL" -> IPosSetter.TOP_RIGHT_HORIZONTAL;
            case "TOP_RIGHT_VERTICAL" -> IPosSetter.TOP_RIGHT_VERTICAL;
            default -> throw new IllegalStateException("Unexpected position preset: " + str);
        };
    }

    @Override
    protected void handleImpl(IBogoSortAPI api) {
        api.addPlayerSortButtonPosition(target(), posSetter);
    }
}
