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

    default void requestFocus() {
        WidgetInstance<?> thisInstance;

        try {
            thisInstance = (WidgetInstance<?>) this;
        } catch (ClassCastException e) {
            throw new ThisOneIsDefinitelyOnYou(e);
        }

        var host = thisInstance.host();
        if (host != null) {
            host.moveFocusTo(this);
        } else {
            WidgetInstance.addPostAttachCallback(thisInstance, () -> thisInstance.host().moveFocusTo(this));
        }
    }
}

class ThisOneIsDefinitelyOnYou extends RuntimeException {
    public ThisOneIsDefinitelyOnYou(Throwable cause) {
        super(cause);
    }
}