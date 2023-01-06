package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.ScrollWidget;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.IntFunction;

public class ListWidget<W extends ListWidget<W>> extends ScrollWidget<W> implements ILayoutWidget {

    public static ListWidget<?> builder(int size, IntFunction<IWidget> function) {
        ListWidget<?> listWidget = new ListWidget<>();
        for (int i = 0; i < size; i++) {
            IWidget child = function.apply(i);
            if (child != null) {
                listWidget.child(child);
            }
        }
        return listWidget;
    }

    public static <T> ListWidget<?> of(Iterable<T> elements, Function<T, IWidget> function) {
        ListWidget<?> listWidget = new ListWidget<>();
        for (T t : elements) {
            IWidget child = function.apply(t);
            if (child != null) {
                listWidget.child(child);
            }
        }
        return listWidget;
    }

    public ListWidget() {
        flex().startDefaultMode()
                .size(1f, 1f)
                .endDefaultMode();
    }

    @Override
    public void layoutWidgets() {
        int y = getArea().getPadding().top;
        for (IWidget child : getChildren()) {
            child.getArea().rx = getArea().getPadding().left;
            child.getArea().ry = y;
            y += child.getArea().requestedHeight();
        }
        getScrollArea().scrollSize = y;
    }

    @Override
    public void onChildAdd(IWidget child) {
        if (isValid()) {
            layoutWidgets();
        }
    }

    @Override
    public void onChildRemove(IWidget child) {
        if (isValid()) {
            layoutWidgets();
        }
    }
}
