package io.wispforest.uwu.client;

import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.uwu.Uwu;
import io.wispforest.uwu.network.UwuNetworkExample;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;

public class UwuClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        UwuNetworkExample.Client.init();

        if (Uwu.WE_TESTEN_HANDSHAKE) {
            OwoNetChannel.create(new Identifier("uwu", "client_only"));

            Uwu.CHANNEL.registerServerbound(WeirdMessage.class, (data, access) -> {

            });

            Uwu.CHANNEL.registerClientbound(WeirdMessage.class, (data, access) -> {

            });
        }
    }

    public record WeirdMessage(int e) { }
}
