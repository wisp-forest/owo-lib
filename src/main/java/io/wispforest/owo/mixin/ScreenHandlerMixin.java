package io.wispforest.owo.mixin;

import io.wispforest.owo.client.screens.LocalPacket;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin implements OwoScreenHandler, OwoScreenHandlerExtension {
    @Shadow private boolean disableSync;
    private final List<SyncedProperty<?>> owo$properties = new ArrayList<>();
    private final Map<Class<?>, LocalPacket<?>> owo$packets = new LinkedHashMap<>();
    private final List<LocalPacket<?>> owo$clientboundPackets = new ArrayList<>();
    private final List<LocalPacket<?>> owo$serverboundPackets = new ArrayList<>();
    private PlayerEntity owo$player;

    @Override
    public <T> SyncedProperty<T> addProperty(Class<T> klass, T initial) {
        var prop = new SyncedProperty<>(owo$properties.size(), klass, initial);
        owo$properties.add(prop);
        return prop;
    }

    @Override
    public PlayerEntity player() {
        return owo$player;
    }

    @Override
    public <R extends Record> void addServerboundPacket(Class<R> klass, Consumer<R> handler) {
        int id = owo$serverboundPackets.size();
        var packet = new LocalPacket<>(id, false, PacketBufSerializer.get(klass), handler);

        owo$serverboundPackets.add(packet);

        if (owo$packets.put(klass, packet) != null) {
            throw new NetworkException(klass + " is already registered as a packet!");
        }
    }

    @Override
    public <R extends Record> void addClientboundPacket(Class<R> klass, Consumer<R> handler) {
        int id = owo$clientboundPackets.size();
        var packet = new LocalPacket<>(id, true, PacketBufSerializer.get(klass), handler);

        owo$clientboundPackets.add(packet);

        if (owo$packets.put(klass, packet) != null) {
            throw new NetworkException(klass + " is already registered as a packet!");
        }
    }

    @Override
    public <R extends Record> void sendPacket(R packet) {
        if (owo$player == null) {
            throw new NetworkException("Tried to send packet before player was attached");
        }

        var type = owo$packets.get(packet.getClass());

        if (type == null) {
            throw new NetworkException("Tried to send unknown packet of type " + packet.getClass());
        }

        PacketByteBuf buf = PacketByteBufs.create();
        type.write(buf, packet);

        if (type.clientbound()) {
            if (!(owo$player instanceof ServerPlayerEntity serverPlayer)) {
                throw new NetworkException("Tried to send clientbound packet on the server");
            }

            ServerPlayNetworking.send(serverPlayer, ScreenInternals.LOCAL_PACKET, buf);
        } else {
            if (!owo$player.world.isClient) {
                throw new NetworkException("Tried to send serverbound packet on the client");
            }

            ClientPlayNetworking.send(ScreenInternals.LOCAL_PACKET, buf);
        }
    }

    @Override
    public void owo$attachToPlayer(PlayerEntity player) {
        owo$player = player;
    }

    @Inject(method = "syncState", at = @At("RETURN"))
    private void syncOnSyncState(CallbackInfo ci) {
        syncProperties();
    }

    @Inject(method = "sendContentUpdates", at = @At("RETURN"))
    private void syncOnSendContentUpdates(CallbackInfo ci) {
        if (disableSync) return;

        syncProperties();
    }

    @Override
    public void owo$readPropertySync(PacketByteBuf buf) {
        int count = buf.readVarInt();

        for (int i = 0; i < count; i++) {
            int idx = buf.readVarInt();
            SyncedProperty<?> property = owo$properties.get(idx);

            property.read(buf);
        }
    }

    @Override
    public void owo$handlePacket(PacketByteBuf buf, boolean clientbound, Executor executor) {
        int id = buf.readVarInt();
        LocalPacket<?> packet = (clientbound ? owo$clientboundPackets : owo$serverboundPackets).get(id);

        packet.readAndSchedule(buf, executor);
    }

    private void syncProperties() {
        if (owo$player == null) return;
        if (!(owo$player instanceof ServerPlayerEntity player)) return;

        int count = 0;

        for (var prop : owo$properties) {
            if (prop.needsSync()) count++;
        }

        if (count == 0) return;

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeVarInt(count);

        for (var prop : owo$properties) {
            if (!prop.needsSync()) continue;

            buf.writeVarInt(prop.index());
            prop.write(buf);
        }

        ServerPlayNetworking.send(player, ScreenInternals.SYNC_PROPERTIES, buf);
    }


}
