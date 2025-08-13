package io.wispforest.owo.braid.widgets.flex;

public enum CrossAxisAlignment {
    /// The start of the cross axis (left for a column, top for a row)
    START,

    /// The end of the cross axis (right for a column, bottom for a row)
    END,

    /// Center across the cross axis
    CENTER,

    /// Force all children to fill the flex's cross axis constraints
    STRETCH;

    @SuppressWarnings("DuplicateBranchesInSwitch")
    double computeChildOffset(double freeSpace) {
        return Math.floor(switch (this) {
            case STRETCH -> 0;
            case START -> 0;
            case CENTER -> freeSpace / 2;
            case END -> freeSpace;
        });
    }
}
