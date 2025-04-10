package io.wispforest.owo.braid.framework.instance;

public interface KeyboardListener {
    void onKeyDown(int keyCode, int modifiers);
    void onKeyUp(int keyCode, int modifiers);
    void onChar(int charCode, int modifiers);

    void onFocusGained();
    void onFocusLost();
}