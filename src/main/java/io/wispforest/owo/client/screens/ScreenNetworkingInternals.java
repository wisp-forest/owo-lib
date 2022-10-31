package io.wispforest.owo.client.screens;

import io.wispforest.owo.Owo;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public class ScreenNetworkingInternals {
    public static final Identifier SYNC_PROPERTIES = new Identifier("owo", "sync_screen_handler_properties");

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void init() {
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
