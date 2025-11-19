package io.wispforest.owo.itemgroup.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

public class DisplayContextUtils {
    public static ItemGroup.DisplayContext createContext(PlayerEntity player) {
        if (player.getWorld().isClient()) {
            return createClientContext();
        } else if (player instanceof ServerPlayerEntity serverPlayer) {
            var featureSet = serverPlayer.getServer().getSaveProperties().getEnabledFeatures();
            var hasPerms = player.isCreativeLevelTwoOp();

            return new ItemGroup.DisplayContext(featureSet, hasPerms, player.getRegistryManager());
        }

        throw new IllegalStateException("Unable to make ItemGroup.DisplayContext due to unknown issue!");
    }

    @Environment(EnvType.CLIENT)
    public static ItemGroup.DisplayContext createClientContext() {
        var client = MinecraftClient.getInstance();
        var player = client.player;

        Objects.requireNonNull(player, "Unable to get the needed client player to setup owo global condensed entries in REI");

        var featureSet = player.networkHandler.getEnabledFeatures();
        var hasPerms = player.isCreativeLevelTwoOp() && client.options.getOperatorItemsTab().getValue();

        return new ItemGroup.DisplayContext(featureSet, hasPerms, player.getRegistryManager());
    }
}
