package io.wispforest.owo.util;

import java.util.HashMap;
import java.util.Map;

public abstract class SupportsFeaturesImpl<S extends SupportsFeatures<S>> implements SupportsFeatures<S>, AutoCloseable {
    private final Map<Key<S, ?>, Object> features = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Key<S, T> key) {
        T value = (T) features.get(key);

        if (value == null) {
            value = key.factory().apply((S) this);
            features.put(key, value);
        }

        return value;
    }

    @Override
    public void close() {
        for (var feature : features.values()) {
            try {
                if (feature instanceof AutoCloseable closeable) {
                    closeable.close();
                }
            } catch (Exception e) {
                throw new RuntimeException("Destroying " + this + "'s features failed", e);
            }
        }
    }
}
