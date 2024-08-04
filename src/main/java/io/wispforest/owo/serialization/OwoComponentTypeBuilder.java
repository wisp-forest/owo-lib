package io.wispforest.owo.serialization;

import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import net.minecraft.core.component.DataComponentType;

public interface OwoComponentTypeBuilder<T> {
    default DataComponentType.Builder<T> endec(Endec<T> endec) {
        return this.endec(endec, SerializationContext.empty());
    }

    default DataComponentType.Builder<T> endec(Endec<T> endec, SerializationContext assumedContext) {
        return ((DataComponentType.Builder<T>) this)
            .persistent(CodecUtils.toCodec(endec, assumedContext))
            .networkSynchronized(CodecUtils.toPacketCodec(endec));
    }
}
