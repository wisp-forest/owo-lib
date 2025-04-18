package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.KeyboardListener;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import org.jetbrains.annotations.Nullable;

public class KeyboardInput extends SingleChildInstanceWidget {

    @Nullable private KeyDownCallback keyDownCallback;
    @Nullable private KeyUpCallback keyUpCallback;
    @Nullable private CharCallback charCallback;
    @Nullable private FocusGainedCallback focusGainedCallback;
    @Nullable private FocusLostCallback focusLostCallback;

    public KeyboardInput(WidgetSetupCallback<KeyboardInput> setupCallback, Widget child) {
        super(child);
        setupCallback.setup(this);
    }

    public KeyboardInput keyDownCallback(@Nullable KeyDownCallback keyDownCallback) {
        this.assertMutable();
        this.keyDownCallback = keyDownCallback;
        return this;
    }

    public @Nullable KeyDownCallback keyDownCallback() {
        return this.keyDownCallback;
    }

    public KeyboardInput keyUpCallback(@Nullable KeyUpCallback keyUpCallback) {
        this.assertMutable();
        this.keyUpCallback = keyUpCallback;
        return this;
    }

    public @Nullable KeyUpCallback keyUpCallback() {
        return this.keyUpCallback;
    }

    public KeyboardInput charCallback(@Nullable CharCallback charCallback) {
        this.assertMutable();
        this.charCallback = charCallback;
        return this;
    }

    public @Nullable CharCallback charCallback() {
        return this.charCallback;
    }

    public KeyboardInput focusGainedCallback(@Nullable FocusGainedCallback focusGainedCallback) {
        this.assertMutable();
        this.focusGainedCallback = focusGainedCallback;
        return this;
    }

    public @Nullable FocusGainedCallback focusGainedCallback() {
        return this.focusGainedCallback;
    }

    public KeyboardInput focusLostCallback(@Nullable FocusLostCallback focusLostCallback) {
        this.assertMutable();
        this.focusLostCallback = focusLostCallback;
        return this;
    }

    public @Nullable FocusLostCallback focusLostCallback() {
        return this.focusLostCallback;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    @FunctionalInterface
    public interface KeyDownCallback {
        void onKeyDown(int keyCode, int modifiers);
    }

    @FunctionalInterface
    public interface KeyUpCallback {
        void onKeyUp(int keyCode, int modifiers);
    }

    @FunctionalInterface
    public interface CharCallback {
        void onChar(int charCode, int modifiers);
    }

    @FunctionalInterface
    public interface FocusGainedCallback {
        void onFocusGained();
    }

    @FunctionalInterface
    public interface FocusLostCallback {
        void onFocusLost();
    }

    public static class Instance extends SingleChildWidgetInstance<KeyboardInput> implements KeyboardListener {

        public Instance(KeyboardInput widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.sizeToChild(constraints, this.child);
        }

        @Override
        public boolean onKeyDown(int keyCode, int modifiers) {
            if (this.widget.keyDownCallback != null) {
                this.widget.keyDownCallback.onKeyDown(keyCode, modifiers);
                return true;
            }

            return false;
        }

        @Override
        public boolean onKeyUp(int keyCode, int modifiers) {
            if (this.widget.keyUpCallback != null) {
                this.widget.keyUpCallback.onKeyUp(keyCode, modifiers);
                return true;
            }

            return false;
        }

        @Override
        public boolean onChar(int charCode, int modifiers) {
            if (this.widget.charCallback != null) {
                this.widget.charCallback.onChar(charCode, modifiers);
                return true;
            }

            return false;
        }

        @Override
        public void onFocusGained() {
            if (this.widget.focusGainedCallback != null) this.widget.focusGainedCallback.onFocusGained();
        }

        @Override
        public void onFocusLost() {
            if (this.widget.focusLostCallback != null) this.widget.focusLostCallback.onFocusLost();
        }
    }
}
