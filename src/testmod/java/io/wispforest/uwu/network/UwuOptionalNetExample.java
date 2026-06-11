package io.wispforest.uwu.network;

import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.neoforge.env.EnvType;
import io.wispforest.owo.neoforge.env.Environment;
//import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.commands.Commands.literal;

public class UwuOptionalNetExample {
    public static final boolean SERVER_CHANNEL_IN_CLIENT = false;
    public static final boolean CLIENT_CHANNEL_IN_SERVER = false;

    public static void init() {
        if (FMLEnvironment.getDist() == Dist.DEDICATED_SERVER || SERVER_CHANNEL_IN_CLIENT) {
            var serverChannel = OwoNetChannel.createOptional(Identifier.fromNamespaceAndPath("uwu", "optional_server"));

            serverChannel.registerClientbound(StringPacket.class, (message, access) -> {
                access.player().sendSystemMessage(Component.nullToEmpty(message.value()));
            });

            NeoForge.EVENT_BUS.<RegisterCommandsEvent>addListener((event) -> {
                var dispatcher = event.getDispatcher(); var access = event.getBuildContext(); var environment = event.getCommandSelection();
                dispatcher.register(literal("test_optional_channels")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();

                            if (serverChannel.canSendToPlayer(player))
                                serverChannel.serverHandle(player).send(new StringPacket("Based™"));

                            return 0;
                        }));
            });

            if (CLIENT_CHANNEL_IN_SERVER) {
                var clientChannel = OwoNetChannel.createOptional(Identifier.fromNamespaceAndPath("uwu", "optional_client"));

                clientChannel.registerServerbound(KeycodePacket.class, (message, access) -> {
                    System.out.println(message.key());
                });
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static final class Client {
        public static final KeyMapping NETWORK_TEST = new KeyMapping("key.uwu.network_opt_test", GLFW.GLFW_KEY_M, KeyMapping.Category.MISC);

        public static void init(IEventBus modBus) {
            var clientChannel = OwoNetChannel.createOptional(Identifier.fromNamespaceAndPath("uwu", "optional_client"));

            clientChannel.registerServerbound(KeycodePacket.class, (message, access) -> {
                System.out.println(message.key());
            });

            modBus.<RegisterKeyMappingsEvent>addListener(event -> event.register(NETWORK_TEST));
            NeoForge.EVENT_BUS.<ClientTickEvent.Post>addListener(event -> {
                while (NETWORK_TEST.consumeClick()) {
                    if (clientChannel.canSendToServer()) {
                        clientChannel.clientHandle().send(new KeycodePacket(NETWORK_TEST.getKey().getValue()));
                    } else {
                        Minecraft.getInstance().player.sendSystemMessage(Component.nullToEmpty("channel unavailable"));
                    }
                }
            });
        }
    }
}
