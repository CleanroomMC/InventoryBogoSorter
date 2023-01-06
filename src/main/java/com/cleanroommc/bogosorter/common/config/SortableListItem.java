package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.widget.DraggableWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SortableListItem<T> extends DraggableWidget<SortableListItem<T>> {

    private final IWidget upButton;
    private final IWidget downButton;
    private final IWidget removeButton;
    private IWidget content;
    private final List<IWidget> allChildren = new ArrayList<>();
    private int currentIndex;
    private SortableListWidget<T> listWidget;
    private final T value;
    private boolean active = true;

    public SortableListItem(T value) {
        this.value = value;
        this.upButton = new ButtonWidget<>()
                .onMouseTapped(mouseButton -> {
                    listWidget.moveElementUp(this.currentIndex);
                    return true;
                })
                .background(GuiTextures.BUTTON)
                .size(10, 10);
        this.downButton = new ButtonWidget<>()
                .onMouseTapped(mouseButton -> {
                    listWidget.moveElementDown(this.currentIndex);
                    return true;
                })
                .background(GuiTextures.BUTTON)
                .size(10, 10);
        this.removeButton = new ButtonWidget<>()
                .onMouseTapped(mouseButton -> {
                    listWidget.removeElement(this.currentIndex);
                    return true;
                })
                .background(GuiTextures.BUTTON)
                .size(10, 20);
    }

    protected void init(SortableListWidget<T> listWidget) {
        this.listWidget = listWidget;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public void onInit() {
        this.content = this.listWidget.getWidgetCreator().apply(this.value);
        makeChildrenList();
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        int w = content.getSize().width + (listWidget.areElementsRemovable() ? 20 : 10);
        int h = Math.max(content.getSize().height, 20);
        Size size = new Size(w, h);

        // need to layout children after sizing because size may be needed for positioning
        if (content.getSize().height >= 20) {
            this.content.setPosSilent(Pos2d.ZERO);
        } else {
            this.content.setPosSilent(new Pos2d(0, size.height / 2 - content.getSize().height / 2));
        }
        this.upButton.setPosSilent(new Pos2d(content.getSize().width, 0));
        this.downButton.setPosSilent(new Pos2d(content.getSize().width, size.height - 10));
        this.removeButton.setSize(10, size.height);
        this.removeButton.setPosSilent(new Pos2d(size.width - 10, 0));

        return size;
    }

    private void makeChildrenList() {
        this.allChildren.clear();
        this.allChildren.add(content);
        this.allChildren.add(upButton);
        this.allChildren.add(downButton);
        if (listWidget.areElementsRemovable()) {
            this.allChildren.add(removeButton);
        }
    }

    @Override
    public boolean canHover() {
        return true;
    }

    @Override
    public void renderMovingState(float partialTicks) {
        Cursor cursor = getContext().getCursor();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-getAbsolutePos().x, -getAbsolutePos().y, 0);
        GlStateManager.translate(cursor.getX() - relativeClickPos.x, cursor.getY() - relativeClickPos.y, 0);
        drawInternal(partialTicks, true);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean onDragStart(int button) {
        setActive(false);
        setEnabled(false);
        relativeClickPos = getContext().getMousePos().subtract(getAbsolutePos());
        return true;
    }

    @Override
    public void onDragEnd(boolean successful) {
        setActive(true);
        setEnabled(true);
        checkNeedsRebuild();
        relativeClickPos = null;
    }

    @Override
    public boolean canDropHere(@Nullable Widget widget, boolean isInBounds) {
        if (widget != null && widget.getParent() instanceof SortableListItem) {
            SortableListItem<T> listItem = (SortableListItem<T>) widget.getParent();
            return value.getClass().isAssignableFrom(listItem.value.getClass()) && currentIndex != listItem.currentIndex;
        }
        return false;
    }

    @Override
    public void onDrag(int mouseButton, long timeSinceLastClick) {
        SortableListItem<T> listItem = findSortableListItem();
        if (listItem != null) {
            listWidget.putAtIndex(currentIndex, listItem.currentIndex);
        }
    }

    @Nullable
    private SortableListItem<T> findSortableListItem() {
        if (!listWidget.isUnderMouse(getContext().getMousePos())) return null;
        for (Object hovered : getContext().getCursor().getAllHovered()) {
            if (hovered instanceof ModularWindow && hovered != getWindow()) return null;
            if (hovered instanceof SortableListItem) {
                if (((SortableListItem<?>) hovered).listWidget == listWidget) {
                    return (SortableListItem<T>) hovered;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isMoving() {
        return moving;
    }

    @Override
    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public T getValue() {
        return value;
    }

    @Override
    public List<Widget> getChildren() {
        return this.allChildren;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
