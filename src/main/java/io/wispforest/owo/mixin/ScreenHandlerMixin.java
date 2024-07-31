package io.wispforest.owo.mixin;

import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.client.screens.OwoScreenHandler;
import io.wispforest.owo.client.screens.ScreenInternals;
import io.wispforest.owo.client.screens.ScreenhandlerMessageData;
import io.wispforest.owo.client.screens.SyncedProperty;
import io.wispforest.owo.network.NetworkException;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin implements OwoScreenHandler, OwoScreenHandlerExtension {

    @Shadow private boolean disableSync;

    private final List<SyncedProperty<?>> owo$properties = new ArrayList<>();

    private final Map<Class<?>, ScreenhandlerMessageData<?>> owo$messages = new LinkedHashMap<>();
    private final List<ScreenhandlerMessageData<?>> owo$clientboundMessages = new ArrayList<>();
    private final List<ScreenhandlerMessageData<?>> owo$serverboundMessages = new ArrayList<>();

    private PlayerEntity owo$player = null;

    @Unique
    private ReflectiveEndecBuilder builder;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void createReflectiveBuilder(ScreenHandlerType type, int syncId, CallbackInfo ci) {
        this.builder = MinecraftEndecs.addDefaults(new ReflectiveEndecBuilder());
    }

    @Override
    public ReflectiveEndecBuilder endecBuilder() {
        return builder;
    }

    @Override
    public void owo$attachToPlayer(PlayerEntity player) {
        this.owo$player = player;
    }

    @Override
    public PlayerEntity player() {
        return this.owo$player;
    }

    @Override
    public <R extends Record> void addServerboundMessage(Class<R> messageClass, Endec<R> endec, Consumer<R> handler) {
        int id = this.owo$serverboundMessages.size();

        var messageData = new ScreenhandlerMessageData<>(id, false, endec, handler);
        this.owo$serverboundMessages.add(messageData);

        if (this.owo$messages.put(messageClass, messageData) != null) {
            throw new NetworkException(messageClass + " is already registered as a message!");
        }
    }

    @Override
    public <R extends Record> void addClientboundMessage(Class<R> messageClass, Endec<R> endec, Consumer<R> handler) {
        int id = this.owo$clientboundMessages.size();

        var messageData = new ScreenhandlerMessageData<>(id, true, endec, handler);
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
        buf.write(messageData.endec(), message);

        var packet = new ScreenInternals.LocalPacket(messageData.id(), buf);

        if (messageData.clientbound()) {
            if (!(this.owo$player instanceof ServerPlayerEntity serverPlayer)) {
                throw new NetworkException("Tried to send clientbound message on the server");
            }

            ServerPlayNetworking.send(serverPlayer, packet);
        } else {
            if (!this.owo$player.getWorld().isClient) {
                throw new NetworkException("Tried to send serverbound message on the client");
            }

            this.owo$sendToServer(packet);
        }
    }

    @Unique
    @OnlyIn(Dist.CLIENT)
    private void owo$sendToServer(CustomPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void owo$handlePacket(ScreenInternals.LocalPacket packet, boolean clientbound) {
        ScreenhandlerMessageData messageData = (clientbound ? this.owo$clientboundMessages : this.owo$serverboundMessages).get(packet.packetId());

        messageData.handler().accept(packet.payload().read(messageData.endec()));
    }

    @Override
    public <T> SyncedProperty<T> createProperty(Class<T> clazz, Endec<T> endec, T initial) {
        var prop = new SyncedProperty<>(this.owo$properties.size(), endec, initial);
        this.owo$properties.add(prop);
        return prop;
    }

    @Override
    public void owo$readPropertySync(ScreenInternals.SyncPropertiesPacket packet) {
        int count = packet.payload().readVarInt();

        for (int i = 0; i < count; i++) {
            int idx = packet.payload().readVarInt();
            this.owo$properties.get(idx).read(packet.payload());
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

    @Unique
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

        ServerPlayNetworking.send(player, new ScreenInternals.SyncPropertiesPacket(buf));
    }

}
