package io.wispforest.owo.serialization;

public interface SelfDescribedDeserializer<T> extends Deserializer<T> {
    <S> void readAny(Serializer<S> visitor);
}
