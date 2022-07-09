package io.wispforest.owo.config;

import io.wispforest.owo.Owo;
import io.wispforest.owo.util.Observable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public record Option<T>(String configName, String key, T defaultValue, Observable<T> events, @Nullable ConfigWrapper.Constraint constraint,
                        BoundField backingField) {

    public void set(T value) {
        if (!this.verifyConstraint(value)) return;

        this.backingField.setValue(value);
        this.events.set(value);
    }

    public T value() {
        return this.events.get();
    }

    @SuppressWarnings("unchecked")
    public Class<T> clazz() {
        return (Class<T>) this.backingField.field().getType();
    }

    @SuppressWarnings("unchecked")
    public void synchronizeWithBackingField() {
        final var fieldValue = (T) this.backingField.getValue();
        if (verifyConstraint(fieldValue)) {
            this.events.set(fieldValue);
        } else {
            this.backingField.setValue(this.events.get());
        }
    }

    public boolean verifyConstraint(T value) {
        if (constraint == null) return true;

        final var matched = constraint.test(value);
        if (!matched) {
            Owo.LOGGER.warn(
                    "Option {} in config '{}' could not be updated, as the given value '{}' does not match its constraint: {}",
                    key, configName, value, constraint.formatted()
            );
        }

        return matched;
    }

    public static final class BoundField {
        private Object owner;
        private final Field field;

        public BoundField(Object owner, Field field) {
            this.owner = owner;
            this.field = field;
        }

        @ApiStatus.Internal
        public void rebind(Object root, String key) {
            if (this.owner == root) return;
            var path = key.substring(1).split("\\.");

            try {
                var owner = root;
                var field = root.getClass().getDeclaredField(path[0]);
                for (int i = 1; i < path.length; i++) {
                    owner = field.get(owner);
                    if (owner != null) {
                        field = owner.getClass().getDeclaredField(path[i]);
                    } else {
                        throw new IllegalStateException("Nested config option containers must never be null");
                    }
                }

                this.owner = owner;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to re-bind config option to field", e);
            }
        }

        public Object getValue() {
            try {
                return this.field.get(this.owner);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not access config option field " + field.getName(), e);
            }
        }

        public void setValue(Object value) {
            try {
                this.field.set(this.owner, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not set config option field " + field.getName(), e);
            }
        }

        public Object owner() {
            return owner;
        }

        public Field field() {
            return field;
        }
    }
}
