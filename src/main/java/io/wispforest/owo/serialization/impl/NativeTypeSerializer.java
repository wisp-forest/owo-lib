package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.*;

import java.util.*;

public class NativeTypeSerializer extends HierarchicalSerializer<Object> {

    public NativeTypeSerializer() {
        super(null);
    }

    @Override
    public Set<SerializationAttribute> attributes() {
        return Set.of();
    }

    @Override
    public <V> void writeOptional(Endec<V> endec, Optional<V> optional) {
        this.frame(encoded -> {
            optional.ifPresent(v -> endec.encode(this, v));
            this.consume(Optional.ofNullable(encoded.require("present optional value")));
        });
    }

    @Override
    public void writeBoolean(boolean value) {
        this.consume(value);
    }

    @Override
    public void writeByte(byte value) {
        this.consume(value);
    }

    @Override
    public void writeShort(short value) {
        this.consume(value);
    }

    @Override
    public void writeInt(int value) {
        this.consume(value);
    }

    @Override
    public void writeLong(long value) {
        this.consume(value);
    }

    @Override
    public void writeFloat(float value) {
        this.consume(value);
    }

    @Override
    public void writeDouble(double value) {
        this.consume(value);
    }

    @Override
    public void writeString(String value) {
        this.consume(value);
    }

    @Override
    public void writeBytes(byte[] bytes) {
        this.consume(bytes);
    }

    @Override
    public void writeVarInt(int value) {
        this.consume(value);
    }

    @Override
    public void writeVarLong(long value) {
        this.consume(value);
    }

    @Override
    public <E> SequenceSerializer<E> sequence(Endec<E> elementEndec, int size) {
        return new Sequence<>(elementEndec);
    }

    @Override
    public <V> MapSerializer<V> map(Endec<V> valueEndec, int size) {
        return new Map<>(valueEndec);
    }

    @Override
    public StructSerializer struct() {
        return new Struct();
    }

    @Override
    public Object result() {
        return this.result;
    }

    private class Sequence<V> implements SequenceSerializer<V> {

        private final Endec<V> elementEndec;
        private final List<Object> result;

        private Sequence(Endec<V> elementEndec) {
            this.elementEndec = elementEndec;
            this.result = new ArrayList<>();
        }

        @Override
        public void element(V element) {
            NativeTypeSerializer.this.frame(encoded -> {
                this.elementEndec.encode(NativeTypeSerializer.this, element);
                this.result.add(encoded.require("sequence element"));
            });
        }

        @Override
        public void end() {
            NativeTypeSerializer.this.consume(this.result);
        }
    }

    private class Map<V> implements MapSerializer<V> {

        private final Endec<V> valueEndec;
        private final java.util.Map<String, Object> result;

        private Map(Endec<V> valueEndec) {
            this.valueEndec = valueEndec;
            this.result = new HashMap<>();
        }

        @Override
        public void entry(String key, V value) {
            NativeTypeSerializer.this.frame(encoded -> {
                this.valueEndec.encode(NativeTypeSerializer.this, value);
                this.result.put(key, encoded.require("map value"));
            });
        }

        @Override
        public void end() {
            NativeTypeSerializer.this.consume(this.result);
        }
    }

    private class Struct implements StructSerializer {

        private final java.util.Map<String, Object> result;

        private Struct() {
            this.result = new HashMap<>();
        }

        @Override
        public <F> StructSerializer field(String name, Endec<F> endec, F value) {
            NativeTypeSerializer.this.frame(encoded -> {
                endec.encode(NativeTypeSerializer.this, value);
                this.result.put(name, encoded.require("struct field"));
            });

            return this;
        }

        @Override
        public void end() {
            NativeTypeSerializer.this.consume(this.result);
        }
    }
}
