package com.cleanroommc.bogosorter.api;

import java.util.Comparator;

public class SortRule<T> implements Comparator<T> {

    private final SortType type;
    private final String key;
    private final Comparator<T> comparator;
    private boolean inverted = false;

    public SortRule(String key, SortType type, Comparator<T> comparator) {
        this.type = type;
        this.key = key;
        this.comparator = comparator;
    }

    public String getKey() {
        return key;
    }

    public String getNameLangKey() {
        return "bogosort.sortrules.item." + key + ".name";
    }

    public String getDescriptionLangKey() {
        return "bogosort.sortrules.item." + key + ".description";
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public int compare(T o1, T o2) {
        return inverted ? comparator.compare(o2, o1) : comparator.compare(o1, o2);
    }

    @Override
    public String toString() {
        return "SortRule{" +
                "key='" + key + '\'' +
                '}';
    }
}
