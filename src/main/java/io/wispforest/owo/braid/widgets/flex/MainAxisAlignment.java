package io.wispforest.owo.braid.widgets.flex;

public enum MainAxisAlignment {
    /// The start of the main axis (top for a column, left for a row)
    START,

    /// The end of the main axis (bottom for a column, right for a row)
    END,

    /// Center in the main axis
    CENTER,

    /// Distribute any remaining space evenly between all children
    SPACE_BETWEEN,

    /// Distribute half of any remaining space equally before the first and
    /// after the last child, and the other half evenly between all children
    SPACE_AROUND,

    /// Distribute any remaining space evenly between all children
    /// as well as before the first and after the last child
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
