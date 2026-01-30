package io.wispforest.owo.braid.util.kdl;

import dev.kdl.KdlNode;
import dev.kdl.KdlValue;
import io.wispforest.endec.*;
import io.wispforest.endec.util.RecursiveDeserializer;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class KdlDeserializer extends RecursiveDeserializer<KdlElement> implements SelfDescribedDeserializer<KdlElement> {

    public final List<KdlMapper> mappers;

    public KdlDeserializer(KdlNode rootNode, List<KdlMapper> mappers) {
        super(new KdlElement.KdlNodeElement(rootNode));
        this.mappers = mappers;
    }

    @Override
    public <S> void readAny(SerializationContext ctx, Serializer<S> visitor) {
        this.decodeElement(ctx, visitor, this.getValue());
    }

    private final Endec<KdlElement> elementEndec = Endec.of(
        this::decodeElement,
        (ctx, deserializer) -> { throw new AssertionError("unreachable"); }
    );

    private void decodeElement(SerializationContext ctx, Serializer<?> visitor, KdlElement element) {
        switch (element) {
            case KdlElement.KdlValueElement(var value) -> {
                if (value.isBoolean()) {
                    visitor.writeBoolean(ctx, (Boolean) value.value());
                } else if (value.isNumber()) {
                    visitor.writeLong(ctx, ((Number) value.value()).longValue());
                } else if (value.isString()) {
                    visitor.writeString(ctx, (String) value.value());
                } else if (value.isNull()) {
                    visitor.writeOptional(ctx, this.elementEndec, Optional.empty());
                } else {
                    throw new UnsupportedOperationException("unknown KDL value type");
                }
            }
            case KdlElement.KdlNodeElement(var node) -> {
                try (var state = visitor.struct()) {
                    node.properties().forEach(entry -> {
                        state.field(entry.getKey(), ctx, this.elementEndec, new KdlElement.KdlValueElement(entry.getValue().getFirst()));
                    });
                    for (var mapper : this.mappers) {
                        if (!mapper.export().apply(node)) {
                            continue;
                        }

                        state.field(mapper.key(), ctx, this.elementEndec, mapper.get().apply(node));
                    }
                }
            }
            case KdlElement.KdlElementList(var elements) -> {
                try (var state = visitor.sequence(ctx, this.elementEndec, elements.size())) {
                    elements.forEach(state::element);
                }
            }
        }
    }

    @Override
    public byte readByte(SerializationContext ctx) {
        return this.expectPrimitive(ctx, Number.class).byteValue();
    }

    @Override
    public short readShort(SerializationContext ctx) {
        return this.expectPrimitive(ctx, Number.class).shortValue();
    }

    @Override
    public int readInt(SerializationContext ctx) {
        return this.expectPrimitive(ctx, Number.class).intValue();
    }

    @Override
    public long readLong(SerializationContext ctx) {
        return this.expectPrimitive(ctx, Number.class).longValue();
    }

    @Override
    public float readFloat(SerializationContext ctx) {
        return this.expectPrimitive(ctx, Number.class).floatValue();
    }

    @Override
    public double readDouble(SerializationContext ctx) {
        return this.expectPrimitive(ctx, Number.class).doubleValue();
    }

    @Override
    public int readVarInt(SerializationContext ctx) {
        return this.expectPrimitive(ctx, Number.class).intValue();
    }

    @Override
    public long readVarLong(SerializationContext ctx) {
        return this.expectPrimitive(ctx, Number.class).longValue();
    }

    @Override
    public boolean readBoolean(SerializationContext ctx) {
        return this.expectPrimitive(ctx, Boolean.class);
    }

    @Override
    public String readString(SerializationContext ctx) {
        return this.expectPrimitive(ctx, String.class);
    }

    @Override
    public byte[] readBytes(SerializationContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> Optional<V> readOptional(SerializationContext ctx, Endec<V> endec) {
        var value = this.getValue();
        return !(value instanceof KdlElement.KdlValueElement(var kdlValue) && kdlValue.isNull())
            ? Optional.of(endec.decode(ctx, this))
            : Optional.empty();
    }

    private <K extends KdlElement> K expectElement(SerializationContext ctx, Class<K> clazz) {
        var value = this.getValue();
        if (!(clazz.isAssignableFrom(value.getClass()))) {
            ctx.throwMalformedInput("Expected a " + KdlElement.KdlValueElement.class.getSimpleName() + ", found a " + value.getClass().getSimpleName());
        }
        return (K) value;
    }

    private <V> V expectPrimitive(SerializationContext ctx, Class<V> clazz) {
        var kdlValue = expectElement(ctx, KdlElement.KdlValueElement.class).value();
        if (!clazz.isAssignableFrom(kdlValue.value().getClass())) {
            ctx.throwMalformedInput("Expected a " + clazz.getSimpleName() + ", found a " + kdlValue.value().getClass().getSimpleName());
        }
        //noinspection unchecked
        return (V) kdlValue.value();
    }

    @Override
    public <E> Deserializer.Sequence<E> sequence(SerializationContext ctx, Endec<E> elementEndec) {
        return new Sequence<>(ctx, elementEndec, expectElement(ctx, KdlElement.KdlElementList.class).elements());
    }

    @Override
    public <V> Map<V> map(SerializationContext ctx, Endec<V> valueEndec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Deserializer.Struct struct(SerializationContext ctx) {
        return new Struct(expectElement(ctx, KdlElement.KdlNodeElement.class).node());
    }

    private class Struct implements Deserializer.Struct {

        public final KdlNode node;

        private Struct(KdlNode node) {
            this.node = node;
        }

        @Override
        public @Nullable <F> F field(String name, SerializationContext ctx, Endec<F> endec, @org.jetbrains.annotations.Nullable Supplier<F> defaultValueFactory) {
            var element = this.tryMap(name);
            if (element == null && this.node.properties().hasProperty(name)) {
                element = new KdlElement.KdlValueElement(node.properties().getValue(name).get());
            }

            if (element == null) {
                if (defaultValueFactory != null) {
                    return defaultValueFactory.get();
                }

                throw new IllegalStateException("Required property " + name + " is missing from serialized data");
            }

            var javaMoment = element;
            return KdlDeserializer.this.frame(
                () -> javaMoment,
                () -> endec.decode(ctx, KdlDeserializer.this)
            );
        }

        private @Nullable KdlElement tryMap(String key) {
            if (key.startsWith(".")) {
                var maybeChild = this.node.children().stream().filter(node -> node.name().equals(key)).findFirst();

                return maybeChild.map(KdlElement.KdlNodeElement::new).orElse(null);
            }

            var mapper = KdlDeserializer.this.mappers.stream().filter(element -> Objects.equals(element.key(), key)).findFirst().orElse(null);
            if (mapper == null) {
                return null;
            }

            return mapper.get().apply(this.node);
        }
    }

    private class Sequence<V> implements Deserializer.Sequence<V> {

        public final SerializationContext ctx;
        public final Endec<V> elementEndec;
        public final Iterator<KdlElement> iterator;
        public final int size;

        private Sequence(SerializationContext ctx, Endec<V> elementEndec, List<KdlElement> elements) {
            this.ctx = ctx;
            this.elementEndec = elementEndec;
            this.iterator = elements.iterator();
            this.size = elements.size();
        }

        @Override
        public int estimatedSize() {
            return this.size;
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public V next() {
            var value = this.iterator.next();

            return KdlDeserializer.this.frame(
                () -> value,
                () -> this.elementEndec.decode(this.ctx, KdlDeserializer.this)
            );
        }
    }
}
