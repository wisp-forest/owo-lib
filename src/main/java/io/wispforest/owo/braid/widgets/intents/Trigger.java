package io.wispforest.owo.braid.widgets.intents;

import io.wispforest.owo.braid.core.KeyModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public sealed interface Trigger {

    boolean isTriggered(int button, @Nullable KeyModifiers modifiers);

    Trigger withModifiers(@Nullable KeyModifiers modifiers);

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
            this(keyCode, KeyModifiers.NONE);
        }

        @Override
        public boolean isTriggered(int button, KeyModifiers modifiers) {
            return this.keyCode == button && (this.modifiers == null || this.modifiers.equals(modifiers));
        }

        @Override
        public Trigger withModifiers(@Nullable KeyModifiers modifiers) {
            return !Objects.equals(this.modifiers, modifiers)
                ? new Key(this.keyCode, modifiers)
                : this;
        }
    }

    record Mouse(int button, @Nullable KeyModifiers modifiers) implements Trigger {

        public Mouse(int button) {
            this(button, KeyModifiers.NONE);
        }

        @Override
        public boolean isTriggered(int button, KeyModifiers modifiers) {
            return this.button == button && (this.modifiers == null || this.modifiers.equals(modifiers));
        }

        @Override
        public Trigger withModifiers(@Nullable KeyModifiers modifiers) {
            return !Objects.equals(this.modifiers, modifiers)
                ? new Mouse(this.button, modifiers)
                : this;
        }
    }
}
