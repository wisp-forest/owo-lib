package io.wispforest.owo.serialization.endec;

import com.mojang.datafixers.util.Either;
import io.wispforest.endec.*;

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
    public void encode(SerializationContext ctx, Serializer<?> serializer, Either<L, R> either) {
        if (serializer instanceof SelfDescribedSerializer<?>) {
            either.ifLeft(left -> this.leftEndec.encode(ctx, serializer, left))
                    .ifRight(right -> this.rightEndec.encode(ctx, serializer, right));
        } else {
            try (var struct = serializer.struct()) {
                struct.field("is_left", ctx, Endec.BOOLEAN, either.left().isPresent());

                either.ifLeft(left -> struct.field("left", ctx, this.leftEndec, left))
                        .ifRight(right -> struct.field("right", ctx, this.rightEndec, right));
            }
        }
    }

    @Override
    public Either<L, R> decode(SerializationContext ctx, Deserializer<?> deserializer) {
        boolean selfDescribing = deserializer instanceof SelfDescribedDeserializer<?>;

        if (selfDescribing) {
            Either<L, R> leftResult = null;
            try {
                leftResult = Either.left(deserializer.tryRead(deserializer1 -> this.leftEndec.decode(ctx, deserializer1)));
            } catch (Exception ignore) {}

            if (!this.exclusive && leftResult != null) return leftResult;

            Either<L, R> rightResult = null;
            try {
                rightResult = Either.right(deserializer.tryRead(deserializer1 -> this.rightEndec.decode(ctx, deserializer1)));
            } catch (Exception ignore) {}

            if (this.exclusive && leftResult != null && rightResult != null) {
                throw new IllegalStateException("Both alternatives read successfully, can not pick the correct one; first: " + leftResult + " second: " + rightResult);
            }

            if (leftResult != null) return leftResult;
            if (rightResult != null) return rightResult;

            throw new IllegalStateException("Neither alternative read successfully");
        } else {
            var struct = deserializer.struct(ctx);

            return (struct.field("is_left", ctx, Endec.BOOLEAN))
                    ? Either.left(struct.field("left", ctx, this.leftEndec))
                    : Either.right(struct.field("right", ctx, this.rightEndec));
        }

    }
}
