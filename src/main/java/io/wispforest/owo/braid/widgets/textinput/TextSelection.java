package io.wispforest.owo.braid.widgets.textinput;

public record TextSelection(int start, int end) {

    public static TextSelection collapsed(int cursorPosition) {
        return new TextSelection(cursorPosition, cursorPosition);
    }

    public int lower() {
        return Math.min(this.start, this.end);
    }

    public int upper() {
        return Math.max(this.start, this.end);
    }

    public boolean collapsed() {
        return this.start == this.end;
    }
}
