package io.wispforest.owo.mixin;

import io.wispforest.owo.client.screens.OwoScreenHandler;
import io.wispforest.owo.client.screens.ScreenNetworkingInternals;
import io.wispforest.owo.client.screens.SyncedProperty;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin implements OwoScreenHandler, OwoScreenHandlerExtension {
    @Shadow private boolean disableSync;
    private final List<SyncedProperty<?>> owo$properties = new ArrayList<>();
    private ServerPlayerEntity owo$player;

    @Override
    public <T> SyncedProperty<T> addProperty(Class<T> klass, T initial) {
        var prop = new SyncedProperty<>(owo$properties.size(), klass, initial);
        owo$properties.add(prop);
        return prop;
    }

    @Override
    public void owo$attachToPlayer(ServerPlayerEntity player) {
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
    private void syncProperties() {
        if (owo$player == null) return;

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

        ServerPlayNetworking.send(owo$player, ScreenNetworkingInternals.SYNC_PROPERTIES, buf);
    }
}
