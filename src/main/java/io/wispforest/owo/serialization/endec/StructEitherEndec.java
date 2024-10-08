package io.wispforest.owo.serialization.endec;

import com.mojang.datafixers.util.Either;
import io.wispforest.endec.*;

public final class StructEitherEndec<L, R> implements StructEndec<Either<L, R>> {

    private final StructEndec<L> leftEndec;
    private final StructEndec<R> rightEndec;

    private final boolean exclusive;

    public StructEitherEndec(StructEndec<L> leftEndec, StructEndec<R> rightEndec, boolean exclusive) {
        this.leftEndec = leftEndec;
        this.rightEndec = rightEndec;

        this.exclusive = exclusive;
    }

    @Override
    public void encodeStruct(SerializationContext ctx, Serializer<?> serializer, Serializer.Struct struct, Either<L, R> either) {
        if (serializer instanceof SelfDescribedSerializer<?>) {
            either.ifLeft(left -> this.leftEndec.encodeStruct(ctx, serializer, struct, left))
                    .ifRight(right -> this.rightEndec.encodeStruct(ctx, serializer, struct, right));
        } else {
            struct.field("is_left", ctx, Endec.BOOLEAN, either.left().isPresent());

            either.ifLeft(left -> this.leftEndec.encodeStruct(ctx, serializer, struct, left))
                    .ifRight(right -> this.rightEndec.encodeStruct(ctx, serializer, struct, right));
        }
    }

    @Override
    public Either<L, R> decodeStruct(SerializationContext ctx, Deserializer<?> deserializer, Deserializer.Struct struct) {
        boolean selfDescribing = deserializer instanceof SelfDescribedDeserializer<?>;

        if (selfDescribing) {
            Either<L, R> leftResult = null;
            try {
                leftResult = Either.left(deserializer.tryRead(deserializer1 -> this.leftEndec.decodeStruct(ctx, deserializer1, struct)));
            } catch (Exception ignore) {}

            if (!this.exclusive && leftResult != null) return leftResult;

            Either<L, R> rightResult = null;
            try {
                rightResult = Either.right(deserializer.tryRead(deserializer1 -> this.rightEndec.decodeStruct(ctx, deserializer1, struct)));
            } catch (Exception ignore) {}

            if (this.exclusive && leftResult != null && rightResult != null) {
                throw new IllegalStateException("Both alternatives read successfully, can not pick the correct one; first: " + leftResult + " second: " + rightResult);
            }

            if (leftResult != null) return leftResult;
            if (rightResult != null) return rightResult;

            throw new IllegalStateException("Neither alternative read successfully");
        } else {
            var isLeft = struct.field("is_left", ctx, Endec.BOOLEAN);

            return isLeft ? Either.left(this.leftEndec.decodeStruct(ctx, deserializer, struct))
                    : Either.right(this.rightEndec.decodeStruct(ctx, deserializer, struct));

        }
    }
}
