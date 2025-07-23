package io.wispforest.owo.braid.widgets.basic.action;

import io.wispforest.owo.braid.core.KeyModifiers;
import org.jetbrains.annotations.Nullable;

public sealed interface Trigger {

    boolean isTriggered(int button, @Nullable KeyModifiers modifiers);

    static Trigger.Key ofKey(int keyCode, @Nullable KeyModifiers modifiers) {
        return new Key(keyCode, modifiers);
    }

    static Trigger.Key ofKey(int keyCode) {
        return new Key(keyCode);
    }

    static Trigger.Mouse ofMouse(int button, @Nullable KeyModifiers modifiers) {
        return new Mouse(button, modifiers);
    }

    static Trigger.Mouse ofMouse(int button) {
        return new Mouse(button);
    }

    record Key(int keyCode, @Nullable KeyModifiers modifiers) implements Trigger {

        public Key(int keyCode) {
            this(keyCode, null);
        }

        @Override
        public boolean isTriggered(int button, @Nullable KeyModifiers modifiers) {
            return this.keyCode == button && (this.modifiers == null || this.modifiers.equals(modifiers));
        }
    }

    record Mouse(int button, @Nullable KeyModifiers modifiers) implements Trigger {

        public Mouse(int button) {
            this(button, null);
        }

        @Override
        public boolean isTriggered(int button, @Nullable KeyModifiers modifiers) {
            return this.button == button && (this.modifiers == null || this.modifiers.equals(modifiers));
        }
    }
}
