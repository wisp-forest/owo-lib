package io.wispforest.owo.serialization;

import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import net.minecraft.component.ComponentType;

public interface OwoComponentTypeBuilder<T> {
    default ComponentType.Builder<T> endec(Endec<T> endec) {
        return this.endec(endec, SerializationContext.empty());
    }

    default ComponentType.Builder<T> endec(Endec<T> endec, SerializationContext assumedContext) {
        return ((ComponentType.Builder<T>) this)
            .codec(CodecUtils.toCodec(endec, assumedContext))
            .packetCodec(CodecUtils.toPacketCodec(endec));
    }
}
