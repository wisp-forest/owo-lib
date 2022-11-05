package io.wispforest.owo.mixin;

import io.wispforest.owo.client.screens.ScreenhandlerMessageData;
import io.wispforest.owo.client.screens.OwoScreenHandler;
import io.wispforest.owo.client.screens.ScreenInternals;
import io.wispforest.owo.client.screens.SyncedProperty;
import io.wispforest.owo.network.NetworkException;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin implements OwoScreenHandler, OwoScreenHandlerExtension {

    @Shadow private boolean disableSync;

    private final List<SyncedProperty<?>> owo$properties = new ArrayList<>();

    private final Map<Class<?>, ScreenhandlerMessageData<?>> owo$messages = new LinkedHashMap<>();
    private final List<ScreenhandlerMessageData<?>> owo$clientboundMessages = new ArrayList<>();
    private final List<ScreenhandlerMessageData<?>> owo$serverboundMessages = new ArrayList<>();

    private PlayerEntity owo$player = null;

    @Override
    public void owo$attachToPlayer(PlayerEntity player) {
        this.owo$player = player;
    }

    @Override
    public PlayerEntity player() {
        return this.owo$player;
    }

    @Override
    public <R extends Record> void addServerboundMessage(Class<R> messageClass, Consumer<R> handler) {
        int id = this.owo$serverboundMessages.size();

        var messageData = new ScreenhandlerMessageData<>(id, false, PacketBufSerializer.get(messageClass), handler);
        this.owo$serverboundMessages.add(messageData);

        if (this.owo$messages.put(messageClass, messageData) != null) {
            throw new NetworkException(messageClass + " is already registered as a message!");
        }
    }

    @Override
    public <R extends Record> void addClientboundMessage(Class<R> messageClass, Consumer<R> handler) {
        int id = this.owo$clientboundMessages.size();

        var messageData = new ScreenhandlerMessageData<>(id, true, PacketBufSerializer.get(messageClass), handler);
        this.owo$clientboundMessages.add(messageData);

        if (this.owo$messages.put(messageClass, messageData) != null) {
            throw new NetworkException(messageClass + " is already registered as a message!");
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <R extends Record> void sendMessage(@NotNull R message) {
        if (this.owo$player == null) {
            throw new NetworkException("Tried to send a message before player was attached");
        }

        ScreenhandlerMessageData messageData = this.owo$messages.get(message.getClass());

        if (messageData == null) {
            throw new NetworkException("Tried to send message of unknown type " + message.getClass());
        }

        var buf = PacketByteBufs.create();
        buf.writeVarInt(messageData.id());
        messageData.serializer().serializer().accept(buf, message);

        if (messageData.clientbound()) {
            if (!(this.owo$player instanceof ServerPlayerEntity serverPlayer)) {
                throw new NetworkException("Tried to send clientbound message on the server");
            }

            ServerPlayNetworking.send(serverPlayer, ScreenInternals.LOCAL_PACKET, buf);
        } else {
            if (!this.owo$player.world.isClient) {
                throw new NetworkException("Tried to send serverbound message on the client");
            }

            ClientPlayNetworking.send(ScreenInternals.LOCAL_PACKET, buf);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void owo$handlePacket(PacketByteBuf buf, boolean clientbound, Executor executor) {
        int id = buf.readVarInt();

        ScreenhandlerMessageData messageData = (clientbound ? this.owo$clientboundMessages : this.owo$serverboundMessages).get(id);

        var message = messageData.serializer().deserializer().apply(buf);
        executor.execute(() -> messageData.handler().accept(message));
    }

    @Override
    public <T> SyncedProperty<T> createProperty(Class<T> klass, T initial) {
        var prop = new SyncedProperty<>(this.owo$properties.size(), klass, initial);
        this.owo$properties.add(prop);
        return prop;
    }

    @Override
    public void owo$readPropertySync(PacketByteBuf buf) {
        int count = buf.readVarInt();

        for (int i = 0; i < count; i++) {
            int idx = buf.readVarInt();
            this.owo$properties.get(idx).read(buf);
        }
    }

    @Inject(method = "syncState", at = @At("RETURN"))
    private void syncOnSyncState(CallbackInfo ci) {
        this.syncProperties();
    }

    @Inject(method = "sendContentUpdates", at = @At("RETURN"))
    private void syncOnSendContentUpdates(CallbackInfo ci) {
        if (disableSync) return;

        this.syncProperties();
    }

    private void syncProperties() {
        if (this.owo$player == null) return;
        if (!(this.owo$player instanceof ServerPlayerEntity player)) return;

        int count = 0;

        for (var property : this.owo$properties) {
            if (property.needsSync()) count++;
        }

        if (count == 0) return;

        var buf = PacketByteBufs.create();
        buf.writeVarInt(count);

        for (var prop : owo$properties) {
            if (!prop.needsSync()) continue;

            buf.writeVarInt(prop.index());
            prop.write(buf);
        }

        ServerPlayNetworking.send(player, ScreenInternals.SYNC_PROPERTIES, buf);
    }

}
