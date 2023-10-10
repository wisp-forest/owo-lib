package io.wispforest.owo.serialization;

public interface SelfDescribedDeserializer<T> extends Deserializer<T> {

    T getEmpty();

    Object any();
}
