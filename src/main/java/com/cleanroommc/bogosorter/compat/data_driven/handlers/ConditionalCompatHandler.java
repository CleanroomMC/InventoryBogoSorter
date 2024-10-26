package com.cleanroommc.bogosorter.compat.data_driven.handlers;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.compat.data_driven.BogoCondition;
import com.cleanroommc.bogosorter.compat.data_driven.BogoCompatHandler;

import java.util.function.Function;

/**
 * @author ZZZank
 */
public class ConditionalCompatHandler extends CompatHandlerBase {
    private final BogoCondition condition;
    private final Function<String, BogoCompatHandler> target2handler;

    public ConditionalCompatHandler(
        String className,
        BogoCondition condition,
        Function<String, BogoCompatHandler> target2handler
    ) {
        super(className);
        this.condition = condition;
        this.target2handler = target2handler;
    }

    @Override
    public void handle(IBogoSortAPI api) {
        if (condition.test()) {
            target2handler.apply(this.targetName);
        }
    }
}
