package com.cleanroommc.bogosorter.common;

public interface Align {

    default int apply(int parentPos, int parentSize, int selfSize) {
        return apply(parentPos, parentSize, selfSize, 0);
    }

    int apply(int parentPos, int parentSize, int selfSize, int offset);

    Align START = (parentPos, parentSize, selfSize, offset) -> parentPos + offset;

    Align END = (parentPos, parentSize, selfSize, offset) -> parentPos + parentSize - selfSize - offset;

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
