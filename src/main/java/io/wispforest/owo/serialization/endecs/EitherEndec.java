package io.wispforest.owo.serialization.endecs;

import com.mojang.datafixers.util.Either;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.Serializer;
import io.wispforest.owo.serialization.impl.SerializationAttribute;

import java.util.Objects;
import java.util.Optional;

public record EitherEndec<F, S>(Endec<F> first, Endec<S> second) implements Endec<Either<F, S>> {

    @Override
    public <E> void encode(Serializer<E> serializer, Either<F, S> either) {
        boolean selfDescribing = serializer.attributes().contains(SerializationAttribute.SELF_DESCRIBING);

        if(!selfDescribing){
            either.ifLeft(left -> {
                try(var struct = serializer.struct()) {
                    struct.field("side", Endec.VAR_INT, 0)
                            .field("value", first, left);
                }
            }).ifRight(right -> {
                try(var struct = serializer.struct()) {
                    struct.field("side", Endec.VAR_INT, 1)
                            .field("value", second, right);
                }
            });

            return;
        }

        either.ifLeft(left -> first.encode(serializer, left))
                .ifRight(right -> second.encode(serializer, right));
    }

    @Override
    public <E> Either<F, S> decode(Deserializer<E> deserializer) {
        boolean selfDescribing = deserializer.attributes().contains(SerializationAttribute.SELF_DESCRIBING);

        if(!selfDescribing){
            var struct = deserializer.struct();

            return switch (struct.field("side", Endec.VAR_INT)){
                case 0 -> Either.left(struct.field("value", first));
                case 1 -> Either.right(struct.field("value", second));
                default -> throw new IllegalStateException("Unknown Int value for given Either Endec");
            };
        }

        Optional<Either<F, S>> result1 = Optional.empty();

        try {
            result1 = Optional.of(Either.left(deserializer.tryRead(first::decode)));
        } catch (Exception ignore) {}

        Optional<Either<F, S>> result2 = Optional.empty();

        try {
            result2 = Optional.of(Either.right(deserializer.tryRead(second::decode)));
        } catch (Exception ignore) {}

        if (result1.isPresent()) return result1.get();
        if (result2.isPresent()) return result2.get();

        throw new IllegalStateException("Neither alternatives read successfully!");
    }

    public boolean equals(Object o) {
        if (this == o) return true;

        if (o != null && this.getClass() == o.getClass()) {
            EitherEndec<?, ?> either = (EitherEndec) o;
            return Objects.equals(this.first, either.first) && Objects.equals(this.second, either.second);
        }

        return false;
    }

    public String toString() {
        return "EitherCodec[" + this.first + ", " + this.second + "]";
    }
}
