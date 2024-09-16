package io.wispforest.owo.serialization.format;

import io.wispforest.endec.SerializationContext;

public interface ContextHolder {
    SerializationContext capturedContext();
}