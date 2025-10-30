package io.wispforest.owo.config.serialization;

public record RawConfigData<E>(ConfigSerializer<E> serializer, E element) {
    public boolean isFrom(ConfigSerializer<?> serializer) {
        return serializer() == serializer;
    }
}
