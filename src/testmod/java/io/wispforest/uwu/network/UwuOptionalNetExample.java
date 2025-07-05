package io.wispforest.uwu.network;

import io.wispforest.owo.network.OwoNetChannel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.server.command.CommandManager.literal;

public class UwuOptionalNetExample {
    public static final boolean SERVER_CHANNEL_IN_CLIENT = false;
    public static final boolean CLIENT_CHANNEL_IN_SERVER = false;

    public static void init() {
        if (FMLLoader.getDist() == Dist.DEDICATED_SERVER || SERVER_CHANNEL_IN_CLIENT) {
            var serverChannel = OwoNetChannel.createOptional(Identifier.of("uwu", "optional_server"));

            serverChannel.registerClientbound(StringPacket.class, (message, access) -> {
                access.player().sendMessage(Text.of(message.value()), false);
            });

            NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent commandEvent) -> {
                var dispatcher = commandEvent.getDispatcher();
                var access = commandEvent.getBuildContext();
                var environment = commandEvent.getCommandSelection();

                dispatcher.register(literal("test_optional_channels")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();

                            if (serverChannel.canSendToPlayer(player))
                                serverChannel.serverHandle(player).send(new StringPacket("Based™"));

                            return 0;
                        }));
            });

            if (CLIENT_CHANNEL_IN_SERVER) {
                var clientChannel = OwoNetChannel.createOptional(Identifier.of("uwu", "optional_client"));

                clientChannel.registerServerbound(KeycodePacket.class, (message, access) -> {
                    System.out.println(message.key());
                });
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Client {
        public static final KeyBinding NETWORK_TEST = new KeyBinding("key.uwu.network_opt_test", GLFW.GLFW_KEY_M, "misc");

        public static void init(IEventBus eventBus) {
            var clientChannel = OwoNetChannel.createOptional(Identifier.of("uwu", "optional_client"));

            clientChannel.registerServerbound(KeycodePacket.class, (message, access) -> {
                System.out.println(message.key());
            });

            eventBus.addListener((RegisterKeyMappingsEvent mappingsEvent) -> {
                mappingsEvent.register(NETWORK_TEST);
            });

            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post clientEvent) -> {
                while (NETWORK_TEST.wasPressed()) {
                    if (clientChannel.canSendToServer()) {
                        clientChannel.clientHandle().send(new KeycodePacket(NETWORK_TEST.getKey().getCode()));
                    } else {
                        MinecraftClient.getInstance().player.sendMessage(Text.of("channel unavailable"), false);
                    }
                }
            });
        }
    }
}
