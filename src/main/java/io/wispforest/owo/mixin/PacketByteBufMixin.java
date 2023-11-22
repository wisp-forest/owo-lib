package io.wispforest.owo.mixin;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.impl.bytebuf.ByteBufDeserializer;
import io.wispforest.owo.serialization.impl.bytebuf.ByteBufSerializer;
import io.wispforest.owo.util.pond.OwoPacketByteBufExtension;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings({"DataFlowIssue", "AddedMixinMembersNamePattern"})
@Mixin(PacketByteBuf.class)
public class PacketByteBufMixin implements OwoPacketByteBufExtension {
    @Override
    public <T> void write(Endec<T> endec, T value) {
        endec.encodeFully(() -> new ByteBufSerializer<>((PacketByteBuf) (Object) this), value);
    }

    @Override
    public <T> T read(Endec<T> endec) {
        return endec.decodeFully(ByteBufDeserializer::new, (PacketByteBuf) (Object) this);
    }
}
