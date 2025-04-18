package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.Listenable;

public class TextEditingController extends Listenable {

    protected String text;
    protected int cursorPosition;

    public TextEditingController(String text) {
        this.text = text;
    }

    public TextEditingController() {
        this("");
    }

    public String text() {
        return this.text;
    }

    public void setText(String text) {
        if (this.text.equals(text)) {
            return;
        }

        this.text = text;
        this.notifyListeners();
    }

    public int cursorPosition() {
        return this.cursorPosition;
    }

    public void setCursorPosition(int cursorPosition) {
        if (this.cursorPosition == cursorPosition) {
            return;
        }

        this.cursorPosition = cursorPosition;
        this.notifyListeners();
    }
}
