package com.glisco.owo.client;

import com.glisco.owo.particles.ParticleSystemPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.jetbrains.annotations.ApiStatus;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class OwoClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ParticleSystemPacket.ID, ParticleSystemPacket::onPacket);
    }
}
