package io.wispforest.uwu.client;

import io.wispforest.uwu.network.UwuNetworkExample;
import net.fabricmc.api.ClientModInitializer;

public class UwuClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        UwuNetworkExample.Client.init();
    }
}
