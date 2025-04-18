package io.wispforest.owo.braid.framework.instance;

public interface KeyboardListener {
    default void onKeyDown(int keyCode, int modifiers) {}
    default void onKeyUp(int keyCode, int modifiers) {}
    default void onChar(int charCode, int modifiers) {}

    default void onFocusGained() {}
    default void onFocusLost() {}
}