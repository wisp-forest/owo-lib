package io.wispforest.owo.braid.widgets.basic.action;

import io.wispforest.owo.braid.core.KeyModifiers;

/// A single option for a step in an action sequence.<br>
/// Can represent either a key or mouse button with modifiers.
///
/// If no modifiers are specified, [KeyModifiers#NONE] is used
public sealed interface Trigger {

    boolean isTriggered(int button, KeyModifiers modifiers);

    static Trigger.Key ofKey(int keyCode, KeyModifiers modifiers) {
        return new Key(keyCode, modifiers);
    }

    /// Create a key `Trigger` with [KeyModifiers#NONE]
    static Trigger.Key ofKey(int keyCode) {
        return new Key(keyCode);
    }

    static Trigger.Mouse ofMouse(int button, KeyModifiers modifiers) {
        return new Mouse(button, modifiers);
    }

    /// Create a mouse `Trigger` with [KeyModifiers#NONE]
    static Trigger.Mouse ofMouse(int button) {
        return new Mouse(button);
    }

    record Key(int keyCode, KeyModifiers modifiers) implements Trigger {

        public Key(int keyCode) {
            this(keyCode, KeyModifiers.NONE);
        }

        @Override
        public boolean isTriggered(int button, KeyModifiers modifiers) {
            return this.keyCode == button && this.modifiers.equals(modifiers);
        }
    }

    record Mouse(int button, KeyModifiers modifiers) implements Trigger {

        public Mouse(int button) {
            this(button, KeyModifiers.NONE);
        }

        @Override
        public boolean isTriggered(int button, KeyModifiers modifiers) {
            return this.button == button && this.modifiers.equals(modifiers);
        }
    }
}
