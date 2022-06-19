package com.cleanroommc.bogosorter.compat.gtce;

public interface IModularSortable {

    void addSortArea(String key, int rowSize);

    int getRowSize(String key);
}
