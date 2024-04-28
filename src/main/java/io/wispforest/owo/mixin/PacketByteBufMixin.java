package io.wispforest.owo.mixin;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SerializationContext;
import io.wispforest.owo.serialization.format.bytebuf.ByteBufDeserializer;
import io.wispforest.owo.serialization.format.bytebuf.ByteBufSerializer;
import io.wispforest.owo.serialization.util.EndecBuffer;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings({"DataFlowIssue", "AddedMixinMembersNamePattern"})
@Mixin(PacketByteBuf.class)
public class PacketByteBufMixin implements EndecBuffer {
    @Override
    public <T> void write(SerializationContext ctx, Endec<T> endec, T value) {
        endec.encodeFully(ctx, () -> ByteBufSerializer.of((PacketByteBuf) (Object) this), value);
    }

    @Override
    public <T> T read(SerializationContext ctx, Endec<T> endec) {
        return endec.decodeFully(ctx, ByteBufDeserializer::of, (PacketByteBuf) (Object) this);
    }
}
