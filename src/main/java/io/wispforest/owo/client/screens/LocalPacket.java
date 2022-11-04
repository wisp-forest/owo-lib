package io.wispforest.owo.client.screens;

import io.wispforest.owo.network.serialization.PacketBufSerializer;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

@ApiStatus.Internal
public record LocalPacket<T>(int id, boolean clientbound, PacketBufSerializer<T> serializer, Consumer<T> handler) {
    public void readAndSchedule(PacketByteBuf buf, Executor executor) {
        T value = serializer.deserializer().apply(buf);

        executor.execute(() -> {
            handler.accept(value);
        });
    }

    @SuppressWarnings("unchecked")
    public void write(PacketByteBuf buf, Object value) {
        buf.writeVarInt(id);
        serializer.serializer().accept(buf, (T) value);
    }
}
