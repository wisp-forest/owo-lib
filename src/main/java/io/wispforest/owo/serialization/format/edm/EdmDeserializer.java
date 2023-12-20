package io.wispforest.owo.serialization.format.edm;

import com.google.common.collect.ImmutableSet;
import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.util.RecursiveDeserializer;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EdmDeserializer extends RecursiveDeserializer<EdmElement<?>> implements SelfDescribedDeserializer<EdmElement<?>> {

    private final Set<SerializationAttribute> attributes;

    public EdmDeserializer(EdmElement<?> serialized, SerializationAttribute... extraAttributes) {
        super(serialized);
        this.attributes = ImmutableSet.<SerializationAttribute>builder()
                .add(SerializationAttribute.SELF_DESCRIBING)
                .add(extraAttributes)
                .build();
    }

    @Override
    public Set<SerializationAttribute> attributes() {
        return this.attributes;
    }

    // ---

    @Override
    public byte readByte() {
        return this.getValue().cast();
    }

    @Override
    public short readShort() {
        return this.getValue().cast();
    }

    @Override
    public int readInt() {
        return this.getValue().cast();
    }

    @Override
    public long readLong() {
        return this.getValue().cast();
    }

    // ---

    @Override
    public float readFloat() {
        return this.getValue().cast();
    }

    @Override
    public double readDouble() {
        return this.getValue().cast();
    }

    // ---

    @Override
    public int readVarInt() {
        return this.readInt();
    }

    @Override
    public long readVarLong() {
        return this.readLong();
    }

    // ---

    @Override
    public boolean readBoolean() {
        return this.getValue().cast();
    }

    @Override
    public String readString() {
        return this.getValue().cast();
    }

    @Override
    public byte[] readBytes() {
        return this.getValue().cast();
    }

    @Override
    public <V> Optional<V> readOptional(Endec<V> endec) {
        var optional = this.getValue().<Optional<EdmElement<?>>>cast();
        if (optional.isPresent()) {
            return this.frame(
                    optional::get,
                    () -> Optional.of(endec.decode(this)),
                    false
            );
        } else {
            return Optional.empty();
        }
    }

    // ---

    @Override
    public <E> Deserializer.Sequence<E> sequence(Endec<E> elementEndec) {
        return new Sequence<>(elementEndec, this.getValue().cast());
    }

    @Override
    public <V> Deserializer.Map<V> map(Endec<V> valueEndec) {
        return new Map<>(valueEndec, this.getValue().cast());
    }

    @Override
    public Deserializer.Struct struct() {
        return new Struct(this.getValue().cast());
    }

    // ---

    @Override
    public <S> void readAny(Serializer<S> visitor) {
        this.visit(visitor, this.getValue());
    }

    private <S> void visit(Serializer<S> visitor, EdmElement<?> value) {
        switch (value.type()) {
            case BYTE -> visitor.writeByte(value.cast());
            case SHORT -> visitor.writeShort(value.cast());
            case INT -> visitor.writeInt(value.cast());
            case LONG -> visitor.writeLong(value.cast());
            case FLOAT -> visitor.writeFloat(value.cast());
            case DOUBLE -> visitor.writeDouble(value.cast());
            case BOOLEAN -> visitor.writeBoolean(value.cast());
            case STRING -> visitor.writeString(value.cast());
            case BYTES -> visitor.writeBytes(value.cast());
            case OPTIONAL ->
                    visitor.writeOptional(Endec.<EdmElement<?>>of(this::visit, deserializer -> null), value.cast());
            case SEQUENCE -> {
                try (var sequence = visitor.sequence(Endec.<EdmElement<?>>of(this::visit, deserializer -> null), value.<List<EdmElement<?>>>cast().size())) {
                    value.<List<EdmElement<?>>>cast().forEach(sequence::element);
                }
            }
            case MAP -> {
                try (var map = visitor.map(Endec.<EdmElement<?>>of(this::visit, deserializer -> null), value.<java.util.Map<String, EdmElement<?>>>cast().size())) {
                    value.<java.util.Map<String, EdmElement<?>>>cast().forEach(map::entry);
                }
            }
        }
    }

    // ---

    private class Sequence<V> implements Deserializer.Sequence<V> {

        private final Endec<V> valueEndec;
        private final Iterator<EdmElement<?>> elements;
        private final int size;

        private Sequence(Endec<V> valueEndec, List<EdmElement<?>> elements) {
            this.valueEndec = valueEndec;

            this.elements = elements.iterator();
            this.size = elements.size();
        }

        @Override
        public int estimatedSize() {
            return this.size;
        }

        @Override
        public boolean hasNext() {
            return this.elements.hasNext();
        }

        @Override
        public V next() {
            return EdmDeserializer.this.frame(
                    this.elements::next,
                    () -> this.valueEndec.decode(EdmDeserializer.this),
                    false
            );
        }
    }

    private class Map<V> implements Deserializer.Map<V> {

        private final Endec<V> valueEndec;
        private final Iterator<java.util.Map.Entry<String, EdmElement<?>>> entries;
        private final int size;

        private Map(Endec<V> valueEndec, java.util.Map<String, EdmElement<?>> entries) {
            this.valueEndec = valueEndec;

            this.entries = entries.entrySet().iterator();
            this.size = entries.size();
        }

        @Override
        public int estimatedSize() {
            return this.size;
        }

        @Override
        public boolean hasNext() {
            return this.entries.hasNext();
        }

        @Override
        public java.util.Map.Entry<String, V> next() {
            var entry = entries.next();
            return EdmDeserializer.this.frame(
                    entry::getValue,
                    () -> java.util.Map.entry(entry.getKey(), this.valueEndec.decode(EdmDeserializer.this)),
                    false
            );
        }
    }

    private class Struct implements Deserializer.Struct {

        private final java.util.Map<String, EdmElement<?>> map;

        private Struct(java.util.Map<String, EdmElement<?>> map) {
            this.map = map;
        }

        @Override
        public <F> @Nullable F field(String name, Endec<F> endec) {
            if (!this.map.containsKey(name)) {
                throw new IllegalStateException("Field '" + name + "' was missing from serialized data, but no default value was provided");
            }
            return EdmDeserializer.this.frame(
                    () -> this.map.get(name),
                    () -> endec.decode(EdmDeserializer.this),
                    true
            );
        }

        @Override
        public <F> @Nullable F field(String name, Endec<F> endec, @Nullable F defaultValue) {
            if (!this.map.containsKey(name)) return defaultValue;
            return EdmDeserializer.this.frame(
                    () -> this.map.get(name),
                    () -> endec.decode(EdmDeserializer.this),
                    true
            );
        }
    }
}
