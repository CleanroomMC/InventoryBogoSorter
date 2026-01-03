package com.cleanroommc.bogosorter.compat;

import com.cleanroommc.bogosorter.ClientEventHandler;
import com.cleanroommc.modularui.screen.ModularPanel;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import org.jetbrains.annotations.NotNull;

public class ModularScreenOverlay extends ModularPanel {

    public ModularScreenOverlay(@NotNull String name) {
        super(name);
        invisible();
        full();
    }

    private boolean handleInput() {
        GuiScreen gui = getScreen().getScreenWrapper().getGuiScreen();
        return gui instanceof GuiContainer gc && ClientEventHandler.handleInput(gc);
    }

    @Override
    public boolean onMousePressed(int mouseButton) {
        return handleInput() || super.onMousePressed(mouseButton);
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        return handleInput() || super.onMouseRelease(mouseButton);
    }

    @Override
    public boolean onKeyPressed(char typedChar, int keyCode) {
        return handleInput() || super.onKeyPressed(typedChar, keyCode);
    }

    @Override
    public boolean onKeyRelease(char typedChar, int keyCode) {
        return handleInput() || super.onKeyRelease(typedChar, keyCode);
    }
}
