package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.utils.ClickData;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class AvailableListItem<T> extends Widget<AvailableListItem<T>> implements ILayoutWidget {

    private final T value;
    private final Widget<?> content;
    private IWidget moveButton;
    private IDrawable[] unavailableTexture;
    private IDrawable[] availableTexture;
    private IntConsumer moveConsumer;
    private boolean available = true;
    private final List<IWidget> children = new ArrayList<>();

    public AvailableListItem(T value, Widget<?> content) {
        this.value = value;
        this.content = content;
    }

    @Override
    public void onInit() {
        moveButton = new ButtonWidget<>()
                .onMousePressed(mouseButton -> {
                    if (isAvailable()) {
                        moveConsumer.accept(mouseButton);
                        setAvailable(false);
                        return true;
                    }
                    return false;
                })
                .background(GuiTextures.BUTTON/*, ModularUITextures.ARROW_RIGHT.withFixedSize(10, 10, 0, 5)*/);
        children.add(content);
        children.add(moveButton);

        if (unavailableTexture == null) {
            unavailableTexture = content.getBackground() == null ? new IDrawable[0] : content.getBackground();
        }
        if (availableTexture == null) {
            availableTexture = content.getBackground() == null ? new IDrawable[0] : content.getBackground();
        }
        content.background(available ? availableTexture : unavailableTexture);
    }

    @Override
    public void layoutWidgets() {
        this.moveButton.getArea().setSize(10, Math.max(content.getArea().height, 20));
        this.moveButton.getArea().rx = content.getArea().width;
        this.moveButton.getArea().ry = content.getArea().height / 2 - moveButton.getArea().height / 2;
    }

    @Override
    public @NotNull List<IWidget> getChildren() {
        return children;
    }

    public boolean isAvailable() {
        return available;
    }

    public AvailableListItem<T> setUnavailableBackground(IDrawable... unavailableTexture) {
        this.unavailableTexture = unavailableTexture;
        return this;
    }

    public AvailableListItem<T> setAvailableBackground(IDrawable... availableTexture) {
        this.availableTexture = availableTexture;
        return this;
    }

    public AvailableListItem<T> setMoveConsumer(IntConsumer moveConsumer) {
        this.moveConsumer = moveConsumer;
        return this;
    }

    public AvailableListItem<T> setAvailable(boolean available) {
        this.available = available;
        if (isValid()) {
            content.background(available ? availableTexture : unavailableTexture);
        }
        return this;
    }
}
