package io.wispforest.owo.braid.widgets.focus;

public enum FocusTraversalDirection {
    NEXT,
    PREVIOUS,

    UP,
    DOWN,
    LEFT,
    RIGHT;

    public FocusTraversalDirection opposite() {
        return switch (this) {
            case NEXT -> PREVIOUS;
            case PREVIOUS -> NEXT;
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}
