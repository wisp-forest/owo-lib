package io.wispforest.owo.serialization.endecs;

import com.mojang.datafixers.util.Either;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.Serializer;
import io.wispforest.owo.serialization.impl.SerializationAttribute;

public final class EitherEndec<L, R> implements Endec<Either<L, R>> {

    private final Endec<L> leftEndec;
    private final Endec<R> rightEndec;

    private final boolean exclusive;

    public EitherEndec(Endec<L> leftEndec, Endec<R> rightEndec, boolean exclusive) {
        this.leftEndec = leftEndec;
        this.rightEndec = rightEndec;

        this.exclusive = exclusive;
    }

    @Override
    public void encode(Serializer<?> serializer, Either<L, R> either) {
        if (serializer.attributes().contains(SerializationAttribute.SELF_DESCRIBING)) {
            either.ifLeft(left -> this.leftEndec.encode(serializer, left)).ifRight(right -> this.rightEndec.encode(serializer, right));
        } else {
            either.ifLeft(left -> {
                try (var struct = serializer.struct()) {
                    struct.field("is_left", Endec.BOOLEAN, true).field("left", this.leftEndec, left);
                }
            }).ifRight(right -> {
                try (var struct = serializer.struct()) {
                    struct.field("is_left", Endec.BOOLEAN, false).field("right", this.rightEndec, right);
                }
            });
        }
    }

    @Override
    public Either<L, R> decode(Deserializer<?> deserializer) {
        boolean selfDescribing = deserializer.attributes().contains(SerializationAttribute.SELF_DESCRIBING);

        if (selfDescribing) {
            Either<L, R> leftResult = null;
            try {
                leftResult = Either.left(deserializer.tryRead(this.leftEndec::decode));
            } catch (Exception ignore) {}

            if (!this.exclusive && leftResult != null) return leftResult;

            Either<L, R> rightResult = null;
            try {
                rightResult = Either.right(deserializer.tryRead(this.rightEndec::decode));
            } catch (Exception ignore) {}

            if (this.exclusive && leftResult != null && rightResult != null) {
                throw new IllegalStateException("Both alternatives read successfully, can not pick the correct one; first: " + leftResult + " second: " + rightResult);
            }

            if (leftResult != null) return leftResult;
            if (rightResult != null) return rightResult;

            throw new IllegalStateException("Neither alternative read successfully");
        } else {
            var struct = deserializer.struct();
            if (struct.field("is_left", Endec.BOOLEAN)) {
                return Either.left(struct.field("left", this.leftEndec));
            } else {
                return Either.right(struct.field("right", this.rightEndec));
            }
        }

    }
}
