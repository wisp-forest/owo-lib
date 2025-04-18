package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.Listenable;

public class TextEditingController extends Listenable {
    public String text = "";
    public CursorPosition cursorPosition = CursorPosition.INITIAL;

    protected boolean focused = false;
    public boolean focused() {
        return this.focused;
    }

    @Override
    protected void notifyListeners() {
        super.notifyListeners();
    }
}
