package io.wispforest.owo.serialization.impl.nbtio;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.MapSerializer;
import io.wispforest.owo.serialization.SequenceSerializer;
import io.wispforest.owo.serialization.StructSerializer;
import io.wispforest.owo.serialization.impl.data.DataOutputSerializer;
import net.minecraft.nbt.*;

import java.io.DataOutput;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BinaryNbtSerializer extends DataOutputSerializer<DataOutput> {

    private boolean writeEndValue = false;

    private final DataOutput output;

    public BinaryNbtSerializer(DataOutput output){
        this.output = output;
    }

    public static BinaryNbtSerializer file(DataOutput output){
        var serializer = new BinaryNbtSerializer(output);

        serializer.writeByte(NbtElement.COMPOUND_TYPE);
        serializer.writeString("");

        serializer.writeEndValue = true;

        return serializer;
    }

    public static BinaryNbtSerializer packet(DataOutput output){
        var serializer = new BinaryNbtSerializer(output);

        serializer.writeByte(NbtElement.COMPOUND_TYPE);

        serializer.writeEndValue = true;

        return serializer;
    }

    @Override
    public DataOutput get() {
        return output;
    }

    @Override
    public DataOutput result() {
        writeByte(NbtElement.END_TYPE);
        return super.result();
    }

    @Override
    public <V> void writeOptional(Endec<V> endec, Optional<V> optional) {
        try(var struct = struct()) {
            struct.field("present", Endec.BOOLEAN, optional.isPresent());

            optional.ifPresent(v -> struct.field("value", endec, v));
        }
    }

    @Override
    public void writeBytes(byte[] bytes) {
        super.writeBytes(bytes);
    }

    @Override
    public <V> MapSerializer<V> map(Endec<V> valueEndec, int size) {
        return new BinaryNbtMapSerializer<>(valueEndec);
    }

    @Override
    public <E> SequenceSerializer<E> sequence(Endec<E> elementEndec, int size) {
        if(size == 0){
            writeByte((byte) 0);
            writeVarInt(0);
        }

        return new BinaryNbtSequenceSerializer<>(elementEndec, size);
    }

    @Override
    public StructSerializer struct() {
        return new BinaryNbtMapSerializer<>(null);
    }

    public class BinaryNbtSequenceSerializer<V> implements SequenceSerializer<V>{

        public boolean writtenTypeData = false;
        private final int size;

        private final Endec<V> valueEndec;

        public BinaryNbtSequenceSerializer(Endec<V> valueEndec, int size) {
            this.valueEndec = valueEndec;
            this.size = size;
        }

        @Override
        public void element(V element) {
            if(!writtenTypeData){
                byte type = getType(element);

                writeByte(type);
                writeVarInt(size);

                writtenTypeData = true;
            }

            this.valueEndec.encode(BinaryNbtSerializer.this, element);
        }

        @Override public void end() {}
    }

    public class BinaryNbtMapSerializer<V> implements StructSerializer, MapSerializer<V> {

        private final Endec<V> valueEndec;

        public BinaryNbtMapSerializer(Endec<V> valueEndec) {
            this.valueEndec = valueEndec;
        }

        @Override
        public void entry(String key, V value) {
            field(key, valueEndec, value);
        }

        @Override
        public <F> StructSerializer field(String name, Endec<F> endec, F value) {
            writeByte(getType(value));
            BinaryNbtSerializer.this.writeString(name);
            endec.encode(BinaryNbtSerializer.this, value);

            return this;
        }

        @Override
        public void end() {
            writeByte((byte) 0);
        }
    }

    private byte getType(Object object) {
        if (object == null) {
            return NbtElement.END_TYPE;
        } else if(object instanceof String){
            return NbtElement.STRING_TYPE;
        } else if(object instanceof Boolean) {
            return NbtElement.BYTE_TYPE;
        } else if(object instanceof Byte) {
            return NbtElement.BYTE_TYPE;
        } else if(object instanceof Short) {
            return NbtElement.SHORT_TYPE;
        } else if(object instanceof Integer) {
            return NbtElement.INT_TYPE;
        } else if(object instanceof Long) {
            return NbtElement.LONG_TYPE;
        } else if(object instanceof Float) {
            return NbtElement.FLOAT_TYPE;
        } else if(object instanceof Double) {
            return NbtElement.DOUBLE_TYPE;
        } else if (object instanceof List) {
            return NbtElement.LIST_TYPE;
        } else if (object instanceof Map) {
            return NbtElement.COMPOUND_TYPE;
        }

        return NbtElement.COMPOUND_TYPE;
    }
}
