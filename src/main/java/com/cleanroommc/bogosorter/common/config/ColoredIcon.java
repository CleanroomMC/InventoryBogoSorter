package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;

public class ColoredIcon implements IDrawable {

    private final UITexture drawable;
    private int color;

    public ColoredIcon(UITexture drawable, int color) {
        this.drawable = drawable;
        this.color = color;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        Color.setGlColor(this.color);
        this.drawable.draw(x, y, width, height);
    }
}
