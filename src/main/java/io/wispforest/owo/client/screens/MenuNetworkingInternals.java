package io.wispforest.owo.client.screens;

import io.wispforest.endec.annotations.IsVarInt;
import io.wispforest.owo.Owo;
import io.wispforest.owo.util.pond.OwoAbstractContainerMenuExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class MenuNetworkingInternals {
    public static void init() {
        Owo.MAIN.registerClientboundDeferred(SyncPropertiesPacket.class);
        Owo.MAIN.registerServerbound(LocalPacket.class, (payload, context) -> {
            handlePacket(payload, context.player(), false);
        });
    }

    private static void handlePacket(LocalPacket payload, Player player, boolean clientbound) {
        var menu = player.containerMenu;

        if (menu == null) {
            Owo.LOGGER.error("Received local packet for null ContainerMenu");
            return;
        }

        ((OwoAbstractContainerMenuExtension) menu).owo$handlePacket(payload, clientbound);
    }

    public record LocalPacket(@IsVarInt int packetId, FriendlyByteBuf payload){ }

    public record SyncPropertiesPacket(FriendlyByteBuf payload) { }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void init() {
            ScreenEvents.AFTER_INIT.register((client, screen, _, _) -> {
                if (screen instanceof MenuAccess<?> handled)
                    ((OwoAbstractContainerMenuExtension) handled.getMenu()).owo$attachToPlayer(client.player);
            });

            Owo.MAIN.registerClientbound(SyncPropertiesPacket.class, (payload, context) -> {
                var menu = context.player().containerMenu;

                if (menu == null) {
                    Owo.LOGGER.error("Received sync properties packet for null ContainerMenu");
                    return;
                }

                ((OwoAbstractContainerMenuExtension) menu).owo$readPropertySync(payload);
            });

            Owo.MAIN.registerClientbound(LocalPacket.class, (payload, context) -> {
                handlePacket(payload, context.player(), true);
            });
        }
    }
}
