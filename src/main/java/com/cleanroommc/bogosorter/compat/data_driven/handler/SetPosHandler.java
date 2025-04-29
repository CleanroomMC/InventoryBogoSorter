package com.cleanroommc.bogosorter.compat.data_driven.handler;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.IPosSetter;
import com.google.gson.JsonObject;

import java.util.Locale;

/**
 * @author ZZZank
 */
public class SetPosHandler extends HandlerBase {
    public static SetPosHandler read(JsonObject o) {
        return new SetPosHandler(
            readClass(o),
            switch (o.get("pos").getAsString().toUpperCase(Locale.ROOT)) {
                case "TOP_RIGHT_HORIZONTAL" -> IPosSetter.TOP_RIGHT_HORIZONTAL;
                case "TOP_RIGHT_VERTICAL" -> IPosSetter.TOP_RIGHT_VERTICAL;
                default -> throw new IllegalStateException("Unexpected position preset: " + o.get("pos"));
            }
        );
    }

    private final IPosSetter posSetter;

    public SetPosHandler(String className, IPosSetter posSetter) {
        super(className);
        this.posSetter = posSetter;
    }

    @Override
    public void handle(IBogoSortAPI api) {
        api.addPlayerSortButtonPosition(toClass(), posSetter);
    }
}
