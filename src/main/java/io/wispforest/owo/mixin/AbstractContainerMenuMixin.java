package io.wispforest.owo.mixin;

import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.client.screens.OwoAbstractContainerMenu;
import io.wispforest.owo.client.screens.MenuNetworkingInternals;
import io.wispforest.owo.client.screens.ScreenhandlerMessageData;
import io.wispforest.owo.client.screens.SyncedProperty;
import io.wispforest.owo.network.NetworkException;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.util.pond.OwoAbstractContainerMenuExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
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

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements OwoAbstractContainerMenu, OwoAbstractContainerMenuExtension {

    @Shadow private boolean suppressRemoteUpdates;

    @Unique private final List<SyncedProperty<?>> properties = new ArrayList<>();

    @Unique private final Map<Class<?>, ScreenhandlerMessageData<?>> messages = new LinkedHashMap<>();
    @Unique private final List<ScreenhandlerMessageData<?>> clientBoundMessages = new ArrayList<>();
    @Unique private final List<ScreenhandlerMessageData<?>> serverBoundMessages = new ArrayList<>();

    @Unique private Player player = null;

    @Unique
    private ReflectiveEndecBuilder builder;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void createReflectiveBuilder(MenuType type, int syncId, CallbackInfo ci) {
        this.builder = MinecraftEndecs.addDefaults(new ReflectiveEndecBuilder());
    }

    @Override
    public ReflectiveEndecBuilder endecBuilder() {
        return builder;
    }

    @Override
    public void owo$attachToPlayer(Player player) {
        this.player = player;
    }

    @Override
    public Player player() {
        return this.player;
    }

    @Override
    public <R extends Record> void addServerboundMessage(Class<R> messageClass, Endec<R> endec, Consumer<R> handler) {
        int id = this.serverBoundMessages.size();

        var messageData = new ScreenhandlerMessageData<>(id, false, endec, handler);
        this.serverBoundMessages.add(messageData);

        if (this.messages.put(messageClass, messageData) != null) {
            throw new NetworkException(messageClass + " is already registered as a message!");
        }
    }

    @Override
    public <R extends Record> void addClientboundMessage(Class<R> messageClass, Endec<R> endec, Consumer<R> handler) {
        int id = this.clientBoundMessages.size();

        var messageData = new ScreenhandlerMessageData<>(id, true, endec, handler);
        this.clientBoundMessages.add(messageData);

        if (this.messages.put(messageClass, messageData) != null) {
            throw new NetworkException(messageClass + " is already registered as a message!");
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <R extends Record> void sendMessage(@NotNull R message) {
        if (this.player == null) {
            throw new NetworkException("Tried to send a message before player was attached");
        }

        ScreenhandlerMessageData messageData = this.messages.get(message.getClass());

        if (messageData == null) {
            throw new NetworkException("Tried to send message of unknown type " + message.getClass());
        }

        var ctx = SerializationContext.attributes(RegistriesAttribute.of(this.player.registryAccess()));
        var buf = PacketByteBufs.create();
        buf.write(ctx, messageData.endec(), message);

        var packet = new MenuNetworkingInternals.LocalPacket(messageData.id(), buf);

        if (messageData.clientbound()) {
            if (!(this.player instanceof ServerPlayer serverPlayer)) {
                throw new NetworkException("Tried to send clientbound message on the server");
            }

            ServerPlayNetworking.send(serverPlayer, packet);
        } else {
            if (!this.player.level().isClientSide()) {
                throw new NetworkException("Tried to send serverbound message on the client");
            }

            this.owo$sendToServer(packet);
        }
    }

    @Unique
    @Environment(EnvType.CLIENT)
    private void owo$sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void owo$handlePacket(MenuNetworkingInternals.LocalPacket packet, boolean clientbound) {
        ScreenhandlerMessageData messageData = (clientbound ? this.clientBoundMessages : this.serverBoundMessages).get(packet.packetId());
        var ctx = SerializationContext.attributes(RegistriesAttribute.of(this.player.registryAccess()));

        messageData.handler().accept(packet.payload().read(ctx, messageData.endec()));
    }

    @Override
    public <T> SyncedProperty<T> createProperty(Class<T> clazz, Endec<T> endec, T initial) {
        var prop = new SyncedProperty<>(this.properties.size(), endec, initial, (AbstractContainerMenu)(Object) this);
        this.properties.add(prop);
        return prop;
    }

    @Override
    public void owo$readPropertySync(MenuNetworkingInternals.SyncPropertiesPacket packet) {
        int count = packet.payload().readVarInt();

        for (int i = 0; i < count; i++) {
            int idx = packet.payload().readVarInt();
            this.properties.get(idx).read(packet.payload());
        }
    }

    @Inject(method = "sendAllDataToRemote", at = @At("RETURN"))
    private void syncOnSyncState(CallbackInfo ci) {
        this.syncProperties();
    }

    @Inject(method = "broadcastChanges", at = @At("RETURN"))
    private void syncOnSendContentUpdates(CallbackInfo ci) {
        if (suppressRemoteUpdates) return;

        this.syncProperties();
    }

    @Unique
    private void syncProperties() {
        if (this.player == null) return;
        if (!(this.player instanceof ServerPlayer player)) return;

        int count = 0;

        for (var property : this.properties) {
            if (property.needsSync()) count++;
        }

        if (count == 0) return;

        var buf = PacketByteBufs.create();
        buf.writeVarInt(count);

        for (var prop : properties) {
            if (!prop.needsSync()) continue;

            buf.writeVarInt(prop.index());
            prop.write(buf);
        }

        ServerPlayNetworking.send(player, new MenuNetworkingInternals.SyncPropertiesPacket(buf));
    }

}
