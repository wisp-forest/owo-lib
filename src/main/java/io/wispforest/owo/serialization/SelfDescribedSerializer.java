package io.wispforest.owo.serialization;

import io.wispforest.owo.serialization.impl.SerializationAttribute;

import java.util.Optional;
import java.util.Set;

public interface SelfDescribedSerializer<T> extends Serializer<T> {

    @Override
    default Set<SerializationAttribute> attributes(){
        return Set.of(SerializationAttribute.SELF_DESCRIBING);
    }

    void empty();

    @Override
    default <V> void writeOptional(Codeck<V> codeck, Optional<V> optional) {
        optional.ifPresentOrElse(v -> codeck.encode(this, v), this::empty);
    }

    void writeAny(Object object);

    class FormatSerializeException extends RuntimeException {
        public FormatSerializeException(String message){
            super(message);
        }

        public FormatSerializeException(String message, Throwable throwable){
            super(message, throwable);
        }
    }

}
