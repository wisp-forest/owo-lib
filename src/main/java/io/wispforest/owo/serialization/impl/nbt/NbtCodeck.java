package io.wispforest.owo.serialization.impl.nbt;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.logging.LogUtils;
import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import net.minecraft.nbt.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;

public final class NbtCodeck implements Codeck<NbtElement> {

    public static NbtCodeck INSTANCE = new NbtCodeck();

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public <E> void encode(Serializer<E> serializer, NbtElement value) {
        if(serializer.attributes().contains(SerializationAttribute.SELF_DESCRIBING)){
            writeAny(serializer, value);

            return;
        }

        try {
            ByteArrayDataOutput stream = ByteStreams.newDataOutput();
            NbtIo.write(value, stream);

            Codeck.BYTE_ARRAY.encode(serializer, stream.toByteArray());
        } catch (IOException e){
            LOGGER.error("Unable to serialize the given NbtElement into the given format!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public <E> NbtElement decode(Deserializer<E> deserializer) {
        if(deserializer instanceof SelfDescribedDeserializer<E> selfDescribedDeserializer){
            return toNbt(selfDescribedDeserializer.readAny());
        }

        byte[] array = Codeck.BYTE_ARRAY.decode(deserializer);

        try {
            ByteArrayDataInput stream = ByteStreams.newDataInput(array);

            return NbtIo.read(stream, NbtTagSizeTracker.ofUnlimitedBytes());
        } catch (IOException e){
            LOGGER.error("Unable to deserialize the given format into the desired NbtElement!");
            throw new RuntimeException(e);
        }
    }

    //--

    private void writeAny(Serializer serializer, NbtElement element){
        switch (element.getType()){
            case NbtElement.END_TYPE -> serializer.writeOptional(this, Optional.empty());
            case NbtElement.BYTE_TYPE -> serializer.writeByte(((AbstractNbtNumber)element).byteValue());
            case NbtElement.SHORT_TYPE -> serializer.writeShort(((AbstractNbtNumber)element).shortValue());
            case NbtElement.INT_TYPE -> serializer.writeInt(((AbstractNbtNumber)element).intValue());
            case NbtElement.LONG_TYPE -> serializer.writeLong(((AbstractNbtNumber)element).longValue());
            case NbtElement.FLOAT_TYPE -> serializer.writeFloat(((AbstractNbtNumber)element).floatValue());
            case NbtElement.DOUBLE_TYPE -> serializer.writeDouble(((AbstractNbtNumber)element).doubleValue());
            case NbtElement.STRING_TYPE -> serializer.writeString(element.asString());
            case NbtElement.BYTE_ARRAY_TYPE, NbtElement.INT_ARRAY_TYPE, NbtElement.LONG_ARRAY_TYPE, NbtElement.LIST_TYPE -> {
                list().encode(serializer, (AbstractNbtList<NbtElement>)element);
            }
            case NbtElement.COMPOUND_TYPE -> map().encode(serializer, ((NbtCompound) element).toMap());
            default -> throw new IllegalStateException("Unknown Object type: " + element);
        };
    }

    private NbtElement toNbt(Object object) {
        if (object == null) {
            return NbtEnd.INSTANCE;
        } else if(object instanceof String value){
            return NbtString.of(value);
        } else if(object instanceof Boolean value) {
            return NbtByte.of(value);
        } else if(object instanceof Byte value) {
            return NbtByte.of(value);
        } else if(object instanceof Short value) {
            return NbtShort.of(value);
        } else if(object instanceof Integer value) {
            return NbtInt.of(value);
        } else if(object instanceof Long value) {
            return NbtLong.of(value);
        } else if(object instanceof Float value) {
            return NbtFloat.of(value);
        } else if(object instanceof Double value) {
            return NbtDouble.of(value);
        } else if (object instanceof List objects ) {
            NbtList array = new NbtList();

            try {
                for (Object o : objects) array.add(this.toNbt(o));
            } catch (UnsupportedOperationException e) {
                throw new IllegalArgumentException("Unable to Serializer a List into Nbt Format due to differing entries types.", e);
            }

            if(array.getHeldType() == NbtElement.BYTE_TYPE){
                return new NbtByteArray((List<Byte>) objects);
            } else if (array.getHeldType() == NbtElement.INT_TYPE){
                return new NbtIntArray((List<Integer>) objects);
            } else if (array.getHeldType() == NbtElement.LONG_TYPE) {
                return new NbtLongArray((List<Long>) objects);
            } else {
                return array;
            }
        } else if (object instanceof Map map) {
            NbtCompound compound = new NbtCompound();

            map.forEach((key, value) -> compound.put((String) key, toNbt(value)));

            return compound;
        } else {
            throw new IllegalStateException("Unknown Object type: " + object);
        }
    }
}
