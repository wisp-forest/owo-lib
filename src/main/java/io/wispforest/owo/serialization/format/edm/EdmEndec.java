package io.wispforest.owo.serialization.format.edm;

import com.google.common.io.ByteStreams;
import io.wispforest.owo.serialization.*;

import java.io.IOException;

public class EdmEndec implements Endec<EdmElement<?>> {

    public static final EdmEndec INSTANCE = new EdmEndec();
    public static final Endec<EdmMap> MAP = INSTANCE.xmap(EdmElement::asMap, edmMap -> edmMap);

    private EdmEndec() {}

    @Override
    public void encode(SerializationContext ctx, Serializer<?> serializer, EdmElement<?> value) {
        if (serializer instanceof SelfDescribedSerializer<?>) {
            new EdmDeserializer(value).readAny(ctx, serializer);
            return;
        }

        try {
            var output = ByteStreams.newDataOutput();
            EdmIo.encode(output, value);

            serializer.writeBytes(ctx, output.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode EDM element in EdmEndec", e);
        }
    }

    @Override
    public EdmElement<?> decode(SerializationContext ctx, Deserializer<?> deserializer) {
        if (deserializer instanceof SelfDescribedDeserializer<?> selfDescribedDeserializer) {
            var nativeSerializer = new EdmSerializer();
            selfDescribedDeserializer.readAny(ctx, nativeSerializer);

            return nativeSerializer.result();
        }

        try {
            return EdmIo.decode(ByteStreams.newDataInput(deserializer.readBytes(ctx)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse EDM element in EdmEndec", e);
        }
    }

}
