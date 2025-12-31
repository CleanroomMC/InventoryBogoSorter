package com.cleanroommc.bogosorter.common;

public interface Align {

    int apply(int parentPos, int parentSize, int selfSize);

    default int apply(int parentPos, int parentSize, int selfSize, int offset) {
        return apply(parentPos, parentSize, selfSize + offset);
    }

    Align START = (parentPos, parentSize, selfSize) -> parentPos;

    Align END = (parentPos, parentSize, selfSize) -> parentPos + parentSize - selfSize;

    enum Corner {

        TOP_LEFT(Align.START, Align.START),
        TOP_RIGHT(Align.END, Align.START),
        BOTTOM_LEFT(Align.START, Align.END),
        BOTTOM_RIGHT(Align.END, Align.END);

        public final Align x;
        public final Align y;

        Corner(Align x, Align y) {
            this.x = x;
            this.y = y;
        }
    }
}
