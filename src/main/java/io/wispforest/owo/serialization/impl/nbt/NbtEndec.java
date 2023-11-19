package io.wispforest.owo.serialization.impl.nbt;

import com.google.common.io.ByteStreams;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SelfDescribedDeserializer;
import io.wispforest.owo.serialization.Serializer;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtTagSizeTracker;

import java.io.IOException;

public final class NbtEndec implements Endec<NbtElement> {

    public static final NbtEndec INSTANCE = new NbtEndec();

    private NbtEndec() {}

    @Override
    public <E> void encode(Serializer<E> serializer, NbtElement value) {
        if (serializer.attributes().contains(SerializationAttribute.SELF_DESCRIBING)) {
            NbtDeserializer.of(value).readAny(serializer);
            return;
        }

        try {
            var output = ByteStreams.newDataOutput();
            NbtIo.write(value, output);

            serializer.writeBytes(output.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode binary NBT in NbtEndec", e);
        }
    }

    @Override
    public <E> NbtElement decode(Deserializer<E> deserializer) {
        if (deserializer instanceof SelfDescribedDeserializer<E> selfDescribedDeserializer) {
            var nbt = NbtSerializer.of();
            selfDescribedDeserializer.readAny(nbt);

            return nbt.result();
        }

        try {
            return NbtIo.read(ByteStreams.newDataInput(deserializer.readBytes()), NbtTagSizeTracker.ofUnlimitedBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse binary NBT in NbtEndec", e);
        }
    }
}
