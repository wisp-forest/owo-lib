package io.wispforest.owo.serialization.endec;

import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SerializationContext;
import io.wispforest.owo.serialization.Serializer;
import io.wispforest.owo.serialization.StructEndec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public sealed class StructField<S, F> permits StructField.Flat {

    protected final String name;
    protected final Endec<F> endec;
    protected final Function<S, F> getter;
    protected final @Nullable Supplier<F> defaultValueFactory;

    public StructField(String name, Endec<F> endec, Function<S, F> getter, @Nullable Supplier<F> defaultValueFactory) {
        this.name = name;
        this.endec = endec;
        this.getter = getter;
        this.defaultValueFactory = defaultValueFactory;
    }

    public StructField(String name, Endec<F> endec, Function<S, F> getter, @Nullable F defaultValue) {
        this(name, endec, getter, () -> defaultValue);
    }

    public StructField(String name, Endec<F> endec, Function<S, F> getter) {
        this(name, endec, getter, (Supplier<F>) null);
    }

    public void encodeField(SerializationContext ctx, Serializer.Struct struct, S instance) {
        struct.field(this.name, ctx, this.endec, this.getter.apply(instance));
    }

    public F decodeField(SerializationContext ctx, Deserializer.Struct struct) {
        return this.defaultValueFactory != null
                ? struct.field(this.name, ctx, this.endec, this.defaultValueFactory.get())
                : struct.field(this.name, ctx, this.endec);
    }

    public static final class Flat<S, F> extends StructField<S, F> {

        public Flat(StructEndec<F> endec, Function<S, F> getter) {
            super("", endec, getter, (Supplier<F>) null);
        }

        private StructEndec<F> endec() {
            return (StructEndec<F>) this.endec;
        }

        @Override
        public void encodeField(SerializationContext ctx, Serializer.Struct struct, S instance) {
            this.endec().encodeStruct(ctx, struct, this.getter.apply(instance));
        }

        @Override
        public F decodeField(SerializationContext ctx, Deserializer.Struct struct) {
            return this.endec().decodeStruct(ctx, struct);
        }
    }
}
