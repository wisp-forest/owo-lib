package io.wispforest.owo.serialization;

import net.minecraft.util.Identifier;

import java.util.function.Function;

public interface IdentifiedData {

    static <T> Function<T, Identifier> optionalIDGetter() {
        return (t) -> (t instanceof IdentifiedData data) ? data.getTypeId() : DispatchedEndec.EMPTY_ID;
    }

    Identifier getTypeId();
}
