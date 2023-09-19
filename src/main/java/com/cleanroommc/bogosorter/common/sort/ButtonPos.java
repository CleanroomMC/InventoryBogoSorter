package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.api.IButtonPos;

import java.util.Objects;

public class ButtonPos implements IButtonPos {

    private boolean enabled = true;
    private int x = 0, y = 0;
    private Alignment alignment = Alignment.BOTTOM_RIGHT;
    private Layout layout = Layout.HORIZONTAL;

    public void reset() {
        this.enabled = false;
        this.x = 0;
        this.y = 0;
        this.alignment = Alignment.BOTTOM_RIGHT;
        this.layout = Layout.HORIZONTAL;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setAlignment(Alignment alignment) {
        this.alignment = Objects.requireNonNull(alignment);
    }

    @Override
    public void setLayout(Layout layout) {
        this.layout = Objects.requireNonNull(layout);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public Alignment getAlignment() {
        return this.alignment;
    }

    @Override
    public Layout getLayout() {
        return this.layout;
    }

    public void applyPos(int guiLeft, int guiTop, ButtonHandler.SortButton sortButton, ButtonHandler.SortButton settingsButton) {
        int s = ButtonHandler.BUTTON_SIZE;
        boolean h = this.layout == Layout.HORIZONTAL;
        switch (this.alignment) {
            case TOP_LEFT: {
                sortButton.x = this.x;
                sortButton.y = this.y;
                break;
            }
            case TOP_RIGHT: {
                sortButton.x = h ? this.x - s - s : this.x - s;
                sortButton.y = this.y;
                break;
            }
            case BOTTOM_LEFT: {
                sortButton.x = this.x;
                sortButton.y = h ? this.y - s : this.y - s - s;
                break;
            }
            case BOTTOM_RIGHT: {
                sortButton.x = h ? this.x - s - s : this.x - s;
                sortButton.y = h ? this.y - s : this.y - s - s;
                break;
            }
        }
        sortButton.x += guiLeft;
        sortButton.y += guiTop;
        if (h) {
            settingsButton.x = sortButton.x + s;
            settingsButton.y = sortButton.y;
        } else {
            settingsButton.x = sortButton.x;
            settingsButton.y = sortButton.y + s;
        }
    }
}
