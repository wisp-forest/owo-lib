package io.wispforest.owo.braid.widgets.flex;

public enum CrossAxisAlignment {
    START,
    END,
    CENTER,
    STRETCH;

    @SuppressWarnings("DuplicateBranchesInSwitch")
    double _computeChildOffset(double freeSpace) {
        return Math.floor(switch (this) {
            case STRETCH -> 0;
            case START -> 0;
            case CENTER -> freeSpace / 2;
            case END -> freeSpace;
        });
    }
}
