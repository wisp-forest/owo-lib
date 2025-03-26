package io.wispforest.owo.config.options;

import io.wispforest.owo.config.base.BoundedAccess;

public sealed interface ReflectiveOption<T> extends OptionControlSpec<T> permits FieldOption, RecordOption {

    BoundedAccess<T> backingAccess();

    default boolean detached() {
        return false;
    }
}
