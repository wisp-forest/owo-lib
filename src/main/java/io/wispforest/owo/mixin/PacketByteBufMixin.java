package io.wispforest.owo.mixin;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.format.bytebuf.ByteBufDeserializer;
import io.wispforest.owo.serialization.format.bytebuf.ByteBufSerializer;
import io.wispforest.owo.serialization.util.EndecBuffer;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings({"DataFlowIssue", "AddedMixinMembersNamePattern"})
@Mixin(PacketByteBuf.class)
public class PacketByteBufMixin implements EndecBuffer {
    @Override
    public <T> void write(Endec<T> endec, T value) {
        endec.encodeFully(() -> ByteBufSerializer.of((PacketByteBuf) (Object) this), value);
    }

    @Override
    public <T> T read(Endec<T> endec) {
        return endec.decodeFully(ByteBufDeserializer::of, (PacketByteBuf) (Object) this);
    }
}
