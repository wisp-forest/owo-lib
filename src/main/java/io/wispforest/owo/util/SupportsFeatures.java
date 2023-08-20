package io.wispforest.owo.util;

import java.util.function.Function;

public interface SupportsFeatures<S extends SupportsFeatures<S>> {
    <T> T get(Key<S, T> key);

    record Key<S extends SupportsFeatures<S>, T>(Function<S, T> factory) { }
}
