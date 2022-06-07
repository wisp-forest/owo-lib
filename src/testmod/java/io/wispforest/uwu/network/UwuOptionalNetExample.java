package io.wispforest.uwu.network;

import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.server.command.CommandManager.literal;

public class UwuOptionalNetExample {
    public static final boolean SERVER_CHANNEL_IN_CLIENT = false;
    public static final boolean CLIENT_CHANNEL_IN_SERVER = false;

    public static void init() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER || SERVER_CHANNEL_IN_CLIENT) {
            var serverChannel = OwoNetChannel.createOptional(new Identifier("uwu", "optional_server"));

            serverChannel.registerClientbound(StringPacket.class, (message, access) -> {
                access.player().sendMessage(Text.of(message.value()), false);
            });

            CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
                dispatcher.register(literal("test_optional_channels")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();

                        if (serverChannel.canSendToPlayer(player))
                            serverChannel.serverHandle(player).send(new StringPacket("Based™"));

                        return 0;
                    }));
            });

            if (CLIENT_CHANNEL_IN_SERVER) {
                var clientChannel = OwoNetChannel.createOptional(new Identifier("uwu", "optional_client"));

                clientChannel.registerServerbound(KeycodePacket.class, (message, access) -> {
                    System.out.println(message.key());
                });
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static final class Client {
        public static final KeyBinding NETWORK_TEST = new KeyBinding("key.uwu.network_opt_test", GLFW.GLFW_KEY_M, "misc");

        public static void init() {
            var clientChannel = OwoNetChannel.createOptional(new Identifier("uwu", "optional_client"));

            clientChannel.registerServerbound(KeycodePacket.class, (message, access) -> {
                System.out.println(message.key());
            });

            KeyBindingHelper.registerKeyBinding(NETWORK_TEST);
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                while (NETWORK_TEST.wasPressed()) {
                    if (clientChannel.canSendToServer()) {
                        clientChannel.clientHandle().send(new KeycodePacket(KeyBindingHelper.getBoundKeyOf(NETWORK_TEST).getCode()));
                    } else {
                        client.player.sendMessage(Text.of("channel unavailable"), false);
                    }
                }
            });
        }
    }
}
