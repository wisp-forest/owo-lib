package io.wispforest.owo.serialization.impl.edm;

import com.google.common.io.ByteStreams;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SelfDescribedDeserializer;
import io.wispforest.owo.serialization.Serializer;
import io.wispforest.owo.serialization.impl.SerializationAttribute;

import java.io.IOException;

public class EdmEndec implements Endec<EdmElement<?>> {

    public static final EdmEndec INSTANCE = new EdmEndec();

    private EdmEndec() {}

    @Override
    public <E> void encode(Serializer<E> serializer, EdmElement<?> value) {
        if (serializer.attributes().contains(SerializationAttribute.SELF_DESCRIBING)) {
            new EdmDeserializer(value).readAny(serializer);
            return;
        }

        try {
            var output = ByteStreams.newDataOutput();
            EdmIo.encode(output, value);

            serializer.writeBytes(output.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode EDM element in EdmEndec", e);
        }
    }

    @Override
    public <E> EdmElement<?> decode(Deserializer<E> deserializer) {
        if (deserializer instanceof SelfDescribedDeserializer<E> selfDescribedDeserializer) {
            var nativeSerializer = new EdmSerializer();
            selfDescribedDeserializer.readAny(nativeSerializer);

            return nativeSerializer.result();
        }

        try {
            return EdmIo.decode(ByteStreams.newDataInput(deserializer.readBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse EDM element in EdmEndec", e);
        }
    }

}
