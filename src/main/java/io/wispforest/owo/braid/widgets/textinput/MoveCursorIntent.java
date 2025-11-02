package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.widgets.intents.Intent;

public record MoveCursorIntent(Direction direction, boolean skipWord, boolean selecting) implements Intent {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
}
