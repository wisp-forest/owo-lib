package io.wispforest.owo.braid.widgets.flex;

public enum MainAxisAlignment {
    START,
    END,
    CENTER,
    SPACE_BETWEEN,
    SPACE_AROUND,
    SPACE_EVENLY;

    @SuppressWarnings("DuplicateBranchesInSwitch")
    double leadingSpace(double freeSpace, int childCount) {
        return Math.floor(switch (this) {
            case START -> 0;
            case END -> freeSpace;
            case CENTER -> freeSpace / 2;
            case SPACE_BETWEEN -> 0;
            case SPACE_AROUND -> freeSpace / childCount / 2;
            case SPACE_EVENLY -> freeSpace / (childCount + 1);
        });
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    double between(double freeSpace, int childCount) {
        return Math.floor(switch (this) {
            case START -> 0;
            case END -> 0;
            case CENTER -> 0;
            case SPACE_BETWEEN -> freeSpace / (childCount - 1);
            case SPACE_AROUND -> freeSpace / childCount;
            case SPACE_EVENLY -> freeSpace / (childCount + 1);
        });
    }
}
