package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widgets.TextWidget;

public class SortRuleWidget extends TextWidget {

    private final SortRule<?> sortRule;

    public SortRuleWidget(SortRule<?> sortRule) {
        super(IKey.lang(sortRule.getNameLangKey()));
        this.sortRule = sortRule;
        background(GuiTextures.BUTTON);
        alignment(Alignment.Center);
        color(Color.WHITE.normal);
        shadow(true);
    }

    @Override
    public int getDefaultHeight() {
        return super.getDefaultHeight() + 6;
    }

    @Override
    public int getDefaultWidth() {
        return super.getDefaultWidth() + 6;
    }

    public SortRule<?> getSortRule() {
        return sortRule;
    }
}
