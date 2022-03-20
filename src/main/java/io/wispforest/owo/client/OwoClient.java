package io.wispforest.owo.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class OwoClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {}
}
