package io.wispforest.uwu.client;

import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.uwu.network.UwuNetworkExample;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;

public class UwuClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        UwuNetworkExample.Client.init();
        OwoNetChannel.create(new Identifier("uwu", "client_only"));
    }
}
