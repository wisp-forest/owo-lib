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