package io.wispforest.owo.config.options;

import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.base.Key;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public sealed class MemoryOption<T> extends OptionBase<T> permits RecordOption {
    private T currentValue;

    public MemoryOption(Identifier configId, Key key, T defaultValue, Class<T> clazz, Type genericType, @Nullable ConfigWrapper.Constraint constraint, T currentValue) {
        super(configId, key, defaultValue, clazz, genericType, constraint);
        this.currentValue = currentValue;
    }

    @Override
    public T value() {
        return currentValue;
    }

    public void set(T t) {
        this.currentValue = t;
    }
}
