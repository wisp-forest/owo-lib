package io.wispforest.owo.serialization;

import io.wispforest.owo.serialization.impl.SerializationAttribute;

import java.util.HashSet;
import java.util.Set;

public interface SelfDescribedDeserializer<T> extends Deserializer<T> {

    @Override
    default Set<SerializationAttribute> attributes(){
        return new HashSet<>(Set.of(SerializationAttribute.SELF_DESCRIBING));
    }

    T getEmpty();

    Object readAny();
}
