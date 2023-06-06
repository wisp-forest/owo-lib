package io.wispforest.owo.client.screens;

import io.wispforest.owo.Owo;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ScreenInternals {
    public static final Identifier LOCAL_PACKET = new Identifier("owo", "local_packet");
    public static final Identifier SYNC_PROPERTIES = new Identifier("owo", "sync_screen_handler_properties");

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(LOCAL_PACKET, (server, player, handler, buf, responseSender) -> {
            buf.retain();
            server.execute(() -> {
                var screenHandler = player.currentScreenHandler;

                if (screenHandler == null) {
                    Owo.LOGGER.error("Received local packet for null ScreenHandler");
                    return;
                }

                ((OwoScreenHandlerExtension) screenHandler).owo$handlePacket(buf, false);
                buf.release();
            });
        });
    }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void init() {
            ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (screen instanceof HandledScreen<?> handled)
                    ((OwoScreenHandlerExtension) handled.getScreenHandler()).owo$attachToPlayer(client.player);
            });

            ClientPlayNetworking.registerGlobalReceiver(LOCAL_PACKET, (client, handler, buf, responseSender) -> {
                if (client.player == null) return;

                buf.retain();
                client.execute(() -> {
                    var screenHandler = client.player.currentScreenHandler;

                    if (screenHandler == null) {
                        Owo.LOGGER.error("Received local packet for null ScreenHandler");
                        return;
                    }

                    ((OwoScreenHandlerExtension) screenHandler).owo$handlePacket(buf, true);
                    buf.release();
                });
            });

            ClientPlayNetworking.registerGlobalReceiver(SYNC_PROPERTIES, (client, handler, buf, responseSender) -> {
                buf.retain();

                client.execute(() -> {
                    if (client.player == null) return;

                    if (client.player.currentScreenHandler == null) {
                        Owo.LOGGER.error("Received sync properties packet for null ScreenHandler");
                        return;
                    }

                    ((OwoScreenHandlerExtension) client.player.currentScreenHandler).owo$readPropertySync(buf);

                    buf.release();
                });
            });
        }
    }
}
