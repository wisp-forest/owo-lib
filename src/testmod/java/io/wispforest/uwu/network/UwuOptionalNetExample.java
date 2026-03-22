package io.wispforest.uwu.network;

import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
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
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER || SERVER_CHANNEL_IN_CLIENT) {
            var serverChannel = OwoNetChannel.createOptional(Identifier.fromNamespaceAndPath("uwu", "optional_server"));

            serverChannel.registerClientbound(StringPacket.class, (message, access) -> {
                access.player().sendSystemMessage(Component.nullToEmpty(message.value()));
            });

            CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
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

        public static void init() {
            var clientChannel = OwoNetChannel.createOptional(Identifier.fromNamespaceAndPath("uwu", "optional_client"));

            clientChannel.registerServerbound(KeycodePacket.class, (message, access) -> {
                System.out.println(message.key());
            });

            KeyMappingHelper.registerKeyMapping(NETWORK_TEST);
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                while (NETWORK_TEST.consumeClick()) {
                    if (clientChannel.canSendToServer()) {
                        clientChannel.clientHandle().send(new KeycodePacket(KeyMappingHelper.getBoundKeyOf(NETWORK_TEST).getValue()));
                    } else {
                        client.player.sendSystemMessage(Component.nullToEmpty("channel unavailable"));
                    }
                }
            });
        }
    }
}
