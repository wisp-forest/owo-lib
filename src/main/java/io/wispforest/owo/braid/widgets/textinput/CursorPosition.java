package io.wispforest.owo.braid.widgets.textinput;

public record CursorPosition(int idx, int col, int row) {
    public static final CursorPosition INITIAL = new CursorPosition(0, 0, 0);
}
