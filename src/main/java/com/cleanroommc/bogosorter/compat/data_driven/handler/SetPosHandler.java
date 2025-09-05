package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.IButtonPos;
import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.compat.data_driven.condition.BogoCondition;
import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import net.minecraft.inventory.Container;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
class SetPosHandler extends HandlerBase {
    public static final Map<String, JsonSchema<IPosSetter>> POS_SETTER_REGISTRY = new LinkedHashMap<>();
    public static final JsonSchema<IPosSetter> POS_SETTER_SCHEMA = JsonSchema.dispatch(POS_SETTER_REGISTRY)
        .extractToDefinitions("button_pos_setter");
    public static final JsonSchema<SetPosHandler> SCHEMA = JsonSchema.object(
        CONDITION_SCHEMA.toOptionalField("condition"),
        TARGET_SCHEMA.toField("target"),
        POS_SETTER_SCHEMA.toField("pos_setter"),
        SetPosHandler::new
    ).describe("Set position of sorting button");

    static {
        POS_SETTER_REGISTRY.put("top_right_horizontal", JsonSchema.CONST(IPosSetter.TOP_RIGHT_HORIZONTAL));
        POS_SETTER_REGISTRY.put("top_right_vertical", JsonSchema.CONST(IPosSetter.TOP_RIGHT_VERTICAL));
        POS_SETTER_REGISTRY.put(
            "custom", JsonSchema.object(
                JsonSchema.BOOL
                    .describe("""
                        If `true`, buttons will be placed next to the first slot.
                        If `false`, buttons will be placed next to the last slot in the first row.""")
                    .toField("at_container_left"),
                JsonSchema.INT.toField("x_offset"),
                JsonSchema.INT.toField("y_offset"),
                JsonSchema.forEnum(IButtonPos.Alignment.class)
                    .describe("How should buttons align to the selected slot")
                    .toOptionalField("alignment", IButtonPos.Alignment.BOTTOM_RIGHT),
                JsonSchema.forEnum(IButtonPos.Layout.class)
                    .describe("Button layout")
                    .toOptionalField("layout", IButtonPos.Layout.HORIZONTAL),
                SetPosHandler::createCustomPosSetter
            )
        );
    }

    private final IPosSetter posSetter;

    protected SetPosHandler(
        Optional<BogoCondition> condition,
        Supplier<Class<? extends Container>> target,
        IPosSetter posSetter
    ) {
        super(condition, target);
        this.posSetter = posSetter;
    }

    private static IPosSetter createCustomPosSetter(
        boolean forLeft,
        int xOffset,
        int yOffset,
        IButtonPos.Alignment alignment,
        IButtonPos.Layout layout
    ) {
        return (slotGroup, buttonPos) -> {
            if (slotGroup.getSlots().size() < slotGroup.getRowSize()) {
                buttonPos.setPos(-1000, -1000);
                return;
            }

            ISlot selectedSlot = slotGroup.getSlots().get(forLeft ? 0 : slotGroup.getRowSize() - 1);
            buttonPos.setAlignment(alignment);
            buttonPos.setLayout(layout);
            buttonPos.setPos(selectedSlot.bogo$getX() + xOffset, selectedSlot.bogo$getY() + yOffset);
        };
    }

    @Override
    protected void handleImpl(IBogoSortAPI api) {
        api.addPlayerSortButtonPosition(target(), posSetter);
    }
}
