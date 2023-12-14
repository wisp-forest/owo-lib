package io.wispforest.owo.serialization.format.edm;

import com.google.common.collect.ImmutableSet;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SerializationAttribute;
import io.wispforest.owo.serialization.Serializer;
import io.wispforest.owo.serialization.util.RecursiveSerializer;

import java.util.*;

public class EdmSerializer extends RecursiveSerializer<EdmElement<?>> {

    private final Set<SerializationAttribute> attributes;

    public EdmSerializer(SerializationAttribute... extraAttributes) {
        super(null);
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
    public void writeByte(byte value) {
        this.consume(EdmElement.wrapByte(value));
    }

    @Override
    public void writeShort(short value) {
        this.consume(EdmElement.wrapShort(value));
    }

    @Override
    public void writeInt(int value) {
        this.consume(EdmElement.wrapInt(value));
    }

    @Override
    public void writeLong(long value) {
        this.consume(EdmElement.wrapLong(value));
    }

    // ---

    @Override
    public void writeFloat(float value) {
        this.consume(EdmElement.wrapFloat(value));
    }

    @Override
    public void writeDouble(double value) {
        this.consume(EdmElement.wrapDouble(value));
    }

    // ---

    @Override
    public void writeVarInt(int value) {
        this.consume(EdmElement.wrapInt(value));
    }

    @Override
    public void writeVarLong(long value) {
        this.consume(EdmElement.wrapLong(value));
    }

    // ---

    @Override
    public void writeBoolean(boolean value) {
        this.consume(EdmElement.wrapBoolean(value));
    }

    @Override
    public void writeString(String value) {
        this.consume(EdmElement.wrapString(value));
    }

    @Override
    public void writeBytes(byte[] bytes) {
        this.consume(EdmElement.wrapBytes(bytes));
    }

    @Override
    public <V> void writeOptional(Endec<V> endec, Optional<V> optional) {
        this.frame(encoded -> {
            optional.ifPresent(v -> endec.encode(this, v));
            this.consume(EdmElement.wrapOptional(Optional.ofNullable(encoded.get())));
        }, false);
    }

    // ---

    @Override
    public <E> Serializer.Sequence<E> sequence(Endec<E> elementEndec, int size) {
        return new Sequence<>(elementEndec);
    }

    @Override
    public <V> Serializer.Map<V> map(Endec<V> valueEndec, int size) {
        return new Map<>(valueEndec);
    }

    @Override
    public Serializer.Struct struct() {
        return new Struct();
    }

    // ---

    private class Sequence<V> implements Serializer.Sequence<V> {

        private final Endec<V> elementEndec;
        private final List<EdmElement<?>> result;

        private Sequence(Endec<V> elementEndec) {
            this.elementEndec = elementEndec;
            this.result = new ArrayList<>();
        }

        @Override
        public void element(V element) {
            EdmSerializer.this.frame(encoded -> {
                this.elementEndec.encode(EdmSerializer.this, element);
                this.result.add(encoded.require("sequence element"));
            }, false);
        }

        @Override
        public void end() {
            EdmSerializer.this.consume(EdmElement.wrapSequence(this.result));
        }
    }

    private class Map<V> implements Serializer.Map<V> {

        private final Endec<V> valueEndec;
        private final java.util.Map<String, EdmElement<?>> result;

        private Map(Endec<V> valueEndec) {
            this.valueEndec = valueEndec;
            this.result = new HashMap<>();
        }

        @Override
        public void entry(String key, V value) {
            EdmSerializer.this.frame(encoded -> {
                this.valueEndec.encode(EdmSerializer.this, value);
                this.result.put(key, encoded.require("map value"));
            }, false);
        }

        @Override
        public void end() {
            EdmSerializer.this.consume(EdmElement.wrapMap(this.result));
        }
    }

    private class Struct implements Serializer.Struct {

        private final java.util.Map<String, EdmElement<?>> result;

        private Struct() {
            this.result = new HashMap<>();
        }

        @Override
        public <F> Serializer.Struct field(String name, Endec<F> endec, F value) {
            EdmSerializer.this.frame(encoded -> {
                endec.encode(EdmSerializer.this, value);
                this.result.put(name, encoded.require("struct field"));
            }, true);

            return this;
        }

        @Override
        public void end() {
            EdmSerializer.this.consume(EdmElement.wrapMap(this.result));
        }
    }
}
