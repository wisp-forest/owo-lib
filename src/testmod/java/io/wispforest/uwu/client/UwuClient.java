package io.wispforest.uwu.client;

import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.ServerParticles;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;

public class UwuClient implements ClientModInitializer {

    public static final Identifier BREAK_BLOCK_PARTICLES = new Identifier("uwu", "break_block");

    @Override
    public void onInitializeClient() {
        ServerParticles.registerClientSideHandler(BREAK_BLOCK_PARTICLES, (client, pos, data) -> {
             client.execute(() -> {
                 ClientParticles.persist();

                 ClientParticles.setParticleCount(30);
                 ClientParticles.spawnLine(ParticleTypes.DRAGON_BREATH, client.world, pos.add(.5, .5, .5), pos.add(.5, 2.5, .5), .015f);

                 ClientParticles.randomizeVelocity(.1);
                 ClientParticles.spawn(ParticleTypes.CLOUD, client.world, pos.add(.5, 2.5, .5), 0);

                 ClientParticles.reset();
             });
        });
    }
}
