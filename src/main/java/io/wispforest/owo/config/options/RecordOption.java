package io.wispforest.owo.config.options;

import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.base.BoundedAccess;
import io.wispforest.owo.config.base.Key;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public final class RecordOption<T> extends MemoryOption<T> implements ReflectiveOption<T> {

    private final BoundedAccess.BoundRecordComponent<T> backingComponent;

    public RecordOption(Identifier configId, Key key, T defaultValue, BoundedAccess.BoundRecordComponent<T> backingComponent, ConfigWrapper.@Nullable Constraint constraint, T currentValue) {
        super(configId, key, defaultValue, (Class<T>) backingComponent.type(), backingComponent.genericType(), constraint, currentValue);

        this.backingComponent = backingComponent;
    }

    @Override
    public BoundedAccess<T> backingAccess() {
        return this.backingComponent;
    }
}
