package io.wispforest.owo.mixin.extras;

import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(targets = "net/minecraft/server/world/ServerChunkLoadingManager$EntityTracker")
public interface EntityTrackerAccessor {
    @Accessor("listeners")
    Set<PlayerAssociatedNetworkHandler> getListeners();
}