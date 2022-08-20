package io.wispforest.uwu.client;

import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import io.wispforest.uwu.Uwu;
import io.wispforest.uwu.network.UwuNetworkExample;
import io.wispforest.uwu.network.UwuOptionalNetExample;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public class UwuClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        UwuNetworkExample.Client.init();
        UwuOptionalNetExample.Client.init();

        final var binding = new KeyBinding("key.uwu.hud_test", GLFW.GLFW_KEY_J, "misc");
        KeyBindingHelper.registerKeyBinding(binding);

        final var hudComponentId = new Identifier("uwu", "test_element");
        final Supplier<Component> hudComponent = () ->
                Containers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(Components.item(Items.DIAMOND.getDefaultStack()).margins(Insets.of(3)))
                        .child(Components.label(Text.literal("epic stuff in hud")))
                        .child(Components.entity(Sizing.fixed(50), EntityType.ALLAY, null))
                        .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                        .padding(Insets.of(5))
                        .surface(Surface.PANEL)
                        .margins(Insets.of(5))
                        .positioning(Positioning.relative(100, 25));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (binding.wasPressed()) {
                if (Hud.hasComponent(hudComponentId)) {
                    Hud.remove(hudComponentId);
                } else {
                    Hud.add(hudComponentId, hudComponent);
                }
            }
        });

        Uwu.CHANNEL.registerClientbound(Uwu.OtherTestMessage.class, (message, access) -> {
            access.player().sendMessage(Text.of("Message '" + message.message() + "' from " + message.pos()), false);
        });

        if (Uwu.WE_TESTEN_HANDSHAKE) {
            OwoNetChannel.create(new Identifier("uwu", "client_only_channel"));

            Uwu.CHANNEL.registerServerbound(WeirdMessage.class, (data, access) -> {
            });
            Uwu.CHANNEL.registerClientbound(WeirdMessage.class, (data, access) -> {
            });

            new ParticleSystemController(new Identifier("uwu", "client_only_particles"));
            Uwu.PARTICLE_CONTROLLER.register(WeirdMessage.class, (world, pos, data) -> {
            });
        }

        Uwu.CUBE.setHandler((world, pos, data) -> {
            ClientParticles.setParticleCount(5);
            ClientParticles.spawnCubeOutline(ParticleTypes.END_ROD, world, pos, 1, .01f);
        });
    }

    public record WeirdMessage(int e) {
    }
}
