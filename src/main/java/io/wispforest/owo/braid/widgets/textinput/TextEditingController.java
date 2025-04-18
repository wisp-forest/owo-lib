package io.wispforest.owo.braid.widgets.textinput;

import java.util.ArrayList;
import java.util.List;

public class TextEditingController {
    public String text = "";
    public CursorPosition cursorPosition = CursorPosition.INITIAL;

    protected boolean focused = false;
    public boolean focused() {
        return this.focused;
    }

    private final List<Runnable> listeners = new ArrayList<>();
    public void addListener(Runnable listener) {
        this.listeners.add(listener);
    }
    public void removeListener(Runnable listener) {
        this.listeners.remove(listener);
    }

    protected void notifyListeners() {
        this.listeners.forEach(Runnable::run);
    }
}
