package com.cleanroommc.invtweaks.api;

import java.util.Comparator;

public class SortRule<T> implements Comparator<T> {

    private final SortType type;
    private final Comparator<T> comparator;
    private boolean inverted = false;

    public SortRule(SortType type, Comparator<T> comparator) {
        this.type = type;
        this.comparator = comparator;
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
}
