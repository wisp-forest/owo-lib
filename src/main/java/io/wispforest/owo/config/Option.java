package io.wispforest.owo.config;

import io.wispforest.owo.Owo;
import io.wispforest.owo.util.Observable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public record Option<T>(String configName, String key, T defaultValue, Observable<T> events, @Nullable ConfigWrapper.Constraint constraint,
                        BoundField backingField) {

    public void set(T value) {
        if (!this.verifyConstraint(value)) return;

        this.events.set(value);
        this.backingField.setValue(value);
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

    public record BoundField(Object owner, Field field) {
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
    }
}
