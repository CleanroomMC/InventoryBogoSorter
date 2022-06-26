package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.modularui.api.ModularUITextures;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.widget.IWidgetParent;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.ButtonWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AvailableListItem<T> extends Widget implements IWidgetParent {

    private final T value;
    private final Widget content;
    private Widget moveButton;
    private IDrawable[] unavailableTexture;
    private IDrawable[] availableTexture;
    private Consumer<ClickData> moveConsumer;
    private boolean available = true;
    private final List<Widget> children = new ArrayList<>();

    public AvailableListItem(T value, Widget content) {
        this.value = value;
        this.content = content;
    }

    @Override
    public void initChildren() {
        moveButton = new ButtonWidget()
                .setOnClick((clickData, widget) -> {
                    if (isAvailable()) {
                        moveConsumer.accept(clickData);
                        setAvailable(false);
                    }
                })
                .setBackground(ModularUITextures.BASE_BUTTON, ModularUITextures.ARROW_RIGHT);
        children.add(content);
        children.add(moveButton);
    }

    @Override
    public void onInit() {
        if (unavailableTexture == null) {
            unavailableTexture = content.getBackground() == null ? new IDrawable[0] : content.getBackground();
        }
        if (availableTexture == null) {
            availableTexture = content.getBackground() == null ? new IDrawable[0] : content.getBackground();
        }
        content.setBackground(available ? availableTexture : unavailableTexture);
    }

    @Override
    public void layoutChildren(int maxWidth, int maxHeight) {
        moveButton.setSize(10, Math.max(content.getSize().height, 20));
        moveButton.setPos(content.getSize().width, content.getSize().height / 2 - moveButton.getSize().height / 2);
        content.setPos(Pos2d.ZERO);
    }

    @Override
    public List<Widget> getChildren() {
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

    public AvailableListItem<T> setMoveConsumer(Consumer<ClickData> moveConsumer) {
        this.moveConsumer = moveConsumer;
        return this;
    }

    public AvailableListItem<T> setAvailable(boolean available) {
        this.available = available;
        if (isInitialised()) {
            content.setBackground(available ? availableTexture : unavailableTexture);
        }
        return this;
    }
}
