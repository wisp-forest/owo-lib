package io.wispforest.owo.mixin;

import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer;
import io.wispforest.endec.format.bytebuf.ByteBufSerializer;
import io.wispforest.endec.util.EndecBuffer;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings({"DataFlowIssue", "AddedMixinMembersNamePattern"})
@Mixin(FriendlyByteBuf.class)
public class FriendlyByteBufMixin implements EndecBuffer {
    @Override
    public <T> void write(SerializationContext ctx, Endec<T> endec, T value) {
        endec.encodeFully(ctx, () -> ByteBufSerializer.of((FriendlyByteBuf) (Object) this), value);
    }

    @Override
    public <T> T read(SerializationContext ctx, Endec<T> endec) {
        return endec.decodeFully(ctx, ByteBufDeserializer::of, (FriendlyByteBuf) (Object) this);
    }
}
