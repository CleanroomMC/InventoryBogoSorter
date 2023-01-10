package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.LocatedWidget;
import com.cleanroommc.modularui.widget.DraggableWidget;
import com.cleanroommc.modularui.widget.sizer.IResizeable;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SortableListItem<T> extends DraggableWidget<SortableListItem<T>> implements ILayoutWidget {

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
        flex().width(1f);
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
        this.content.flex().relative(this.listWidget);
        makeChildrenList();
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
    public boolean canDropHere(int x, int y, @Nullable IGuiElement widget) {
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
        if (!listWidget.getArea().isInside(getContext().getMouseX(), getContext().getMouseY())) return null;
        for (LocatedWidget widget : getPanel().getHovering()) {
            if (widget.getWidget() instanceof SortableListItem) {
                if (((SortableListItem<?>) widget.getWidget()).listWidget == listWidget) {
                    return (SortableListItem<T>) widget.getWidget();
                }
            }
        }
        return null;
    }

    @Override
    public void setMoving(boolean moving) {
        super.setMoving(moving);
        setEnabled(!moving);
        setActive(!moving);
    }

    public T getValue() {
        return value;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void resize() {
        this.upButton.resize();
        this.downButton.resize();
        this.removeButton.resize();
        this.content.resize();

        IResizeable resizer = this.resizer();
        if (resizer != null) {
            if (resizer.isSkip()) {
                return;
            }

            resizer.apply(this);
        }

        layoutWidgets();

        if (resizer != null) {
            resizer.postApply(this);
        }

        postLayoutWidgets();
    }

    @Override
    public int getDefaultHeight() {
        return Math.max(content.getArea().height, 20);
    }

    @Override
    public void layoutWidgets() {
        // need to layout children after sizing because size may be needed for positioning
        if (content.getArea().height >= 20) {
            this.content.getArea().rx = 0;
            this.content.getArea().ry = 0;
        } else {
            this.content.getArea().rx = 0;
            this.content.getArea().ry = getArea().height / 2 - content.getArea().height / 2;
        }
        this.upButton.getArea().rx = this.content.getArea().width;
        this.downButton.getArea().rx = this.content.getArea().width;
        this.downButton.getArea().ry = getArea().height - 10;
        this.removeButton.getArea().rx = getArea().width - 10;
    }
}
