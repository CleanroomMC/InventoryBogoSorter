package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widgets.TextWidget;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SortableListWidget<T> extends ListWidget<SortableListWidget<T>> {

    private final Map<T, SortableListItem<T>> widgetMap = new HashMap<>();
    private final List<T> startValues;
    private Function<T, IWidget> widgetCreator = t -> new TextWidget(IKey.str(t.toString()));
    private Consumer<List<T>> saveFunction = list -> {
    };
    private Consumer<T> onRemoveElement = t -> {
    };
    private boolean elementsRemovable = false;

    private boolean initLayout = true;

    public static <T> SortableListWidget<T> removable(Collection<T> allValues, List<T> startValues) {
        return new SortableListWidget<>(true, allValues, startValues);
    }

    private SortableListWidget(boolean removable, Collection<T> allValues, List<T> startValues) {
        this.elementsRemovable = removable;
        for (T t : allValues) {
            widgetMap.put(t, null);
        }
        this.startValues = new ArrayList<>(startValues);
    }

    public SortableListWidget(List<T> startValues) {
        this(false, startValues, startValues);
    }

    @Override
    public void onInit() {
        super.onInit();
        int i = 0;
        for (T t : widgetMap.keySet()) {
            SortableListItem<T> listItem = new SortableListItem<>(t);
            listItem.init(this);
            listItem.setCurrentIndex(i++);
            this.widgetMap.put(t, listItem);
            addChild(listItem, -1);
        }
    }

    @Override
    public void layoutWidgets() {
        super.layoutWidgets();
        if (this.initLayout) {
            this.initLayout = false;
            this.getChildren().clear();
            for (T t : startValues) {
                SortableListItem<T> listItem = widgetMap.get(t);
                if (listItem == null) {
                    BogoSorter.LOGGER.error("Unexpected error: Could not find sortable list item for {}!", t);
                    continue;
                }
                addChild(listItem, -1);
            }
            assignIndexes();
            layoutWidgets();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        this.saveFunction.accept(createElements());
    }

    public List<T> createElements() {
        return getChildren().stream().map(widget -> ((SortableListItem<T>) widget).getValue()).collect(Collectors.toList());
    }

    protected void removeElement(int index) {
        SortableListItem<T> item = (SortableListItem<T>) getChildren().remove(index);
        onRemoveElement.accept(item.getValue());
        assignIndexes();
        layoutWidgets();
    }

    protected void moveElementUp(int index) {
        if (index > 0) {
            IWidget widget = getChildren().remove(index);
            getChildren().add(index - 1, widget);
            assignIndexes();
            layoutWidgets();
        }
    }

    protected void moveElementDown(int index) {
        if (index < getChildren().size() - 1) {
            IWidget widget = getChildren().remove(index);
            getChildren().add(index + 1, widget);
            assignIndexes();
            layoutWidgets();
        }
    }

    protected void putAtIndex(int index, int toIndex) {
        IWidget widget = getChildren().remove(index);
        getChildren().add(toIndex, widget);
        assignIndexes();
        //checkNeedsRebuild();
    }

    protected void assignIndexes() {
        for (int i = 0; i < getChildren().size(); i++) {
            ((SortableListItem<T>) getChildren().get(i)).setCurrentIndex(i);
        }
    }

    public Function<T, IWidget> getWidgetCreator() {
        return widgetCreator;
    }

    public Consumer<T> getOnRemoveElement() {
        return onRemoveElement;
    }

    public boolean areElementsRemovable() {
        return elementsRemovable;
    }

    public void addElement(T t) {
        if (!isValid()) {
            throw new IllegalStateException("List needs to be initialised to add elements dynamically.");
        }
        if (!widgetMap.containsKey(t)) {
            throw new NoSuchElementException("This list widget was not initialised with the value " + t);
        }
        SortableListItem<T> listItem = widgetMap.get(t);
        listItem.setActive(true);
        listItem.setEnabled(true);
        super.addChild(listItem, -1);
        assignIndexes();
        layoutWidgets();
    }

    public SortableListWidget<T> setWidgetCreator(Function<T, IWidget> widgetCreator) {
        this.widgetCreator = widgetCreator;
        return this;
    }

    public SortableListWidget<T> setSaveFunction(Consumer<List<T>> saveFunction) {
        this.saveFunction = saveFunction;
        return this;
    }

    public SortableListWidget<T> setElementsRemovable(boolean elementsRemovable) {
        this.elementsRemovable = elementsRemovable;
        return this;
    }

    public SortableListWidget<T> setOnRemoveElement(Consumer<T> onRemoveElement) {
        this.onRemoveElement = onRemoveElement;
        return this;
    }
}
