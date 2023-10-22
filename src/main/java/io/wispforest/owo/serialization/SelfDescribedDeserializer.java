package io.wispforest.owo.serialization;

public interface SelfDescribedDeserializer<T> extends Deserializer<T> {
    Object readAny();
}
