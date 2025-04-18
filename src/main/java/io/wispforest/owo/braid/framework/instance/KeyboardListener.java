package io.wispforest.owo.braid.framework.instance;

public interface KeyboardListener {
    default boolean onKeyDown(int keyCode, int modifiers) {
        return false;
    }
    default boolean onKeyUp(int keyCode, int modifiers) {
        return false;
    }
    default boolean onChar(int charCode, int modifiers) {
        return false;
    }

    default void onFocusGained() {}
    default void onFocusLost() {}
}