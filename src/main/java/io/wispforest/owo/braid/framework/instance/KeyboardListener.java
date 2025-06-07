package io.wispforest.owo.braid.framework.instance;

import io.wispforest.owo.braid.core.KeyModifiers;

public interface KeyboardListener {
    default boolean onKeyDown(int keyCode, KeyModifiers modifiers) {
        return false;
    }
    default boolean onKeyUp(int keyCode, KeyModifiers modifiers) {
        return false;
    }
    default boolean onChar(int charCode, KeyModifiers modifiers) {
        return false;
    }

    default void onFocusGained() {}
    default void onFocusLost() {}
}
