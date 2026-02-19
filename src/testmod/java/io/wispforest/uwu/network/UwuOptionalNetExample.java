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

import static net.minecraft.commands.Commands.literal;

public class UwuOptionalNetExample {
    public static final boolean SERVER_CHANNEL_IN_CLIENT = false;
    public static final boolean CLIENT_CHANNEL_IN_SERVER = false;

    public static void init() {
        if (FMLLoader.getCurrent().getDist() == Dist.DEDICATED_SERVER || SERVER_CHANNEL_IN_CLIENT) {
            var serverChannel = OwoNetChannel.createOptional(Identifier.fromNamespaceAndPath("uwu", "optional_server"));

            serverChannel.registerClientbound(StringPacket.class, (message, access) -> {
                access.player().displayClientMessage(Component.nullToEmpty(message.value()), false);
            });

            NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent commandEvent) -> {
                var dispatcher = commandEvent.getDispatcher();
                var access = commandEvent.getBuildContext();
                var environment = commandEvent.getCommandSelection();

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

    //@OnlyIn(Dist.CLIENT)
    public static final class Client {
        public static final KeyMapping NETWORK_TEST = new KeyMapping("key.uwu.network_opt_test", GLFW.GLFW_KEY_M, KeyMapping.Category.MISC);

        public static void init(IEventBus eventBus) {
            var clientChannel = OwoNetChannel.createOptional(Identifier.fromNamespaceAndPath("uwu", "optional_client"));

            clientChannel.registerServerbound(KeycodePacket.class, (message, access) -> {
                System.out.println(message.key());
            });

            eventBus.addListener((RegisterKeyMappingsEvent mappingsEvent) -> {
                mappingsEvent.register(NETWORK_TEST);
            });

            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post clientEvent) -> {
                while (NETWORK_TEST.consumeClick()) {
                    if (clientChannel.canSendToServer()) {
                        clientChannel.clientHandle().send(new KeycodePacket(NETWORK_TEST.getKey().getValue()));
                    } else {
                        MinecraftClient.getInstance().player.displayClientMessage(Component.nullToEmpty("channel unavailable"), false);
                    }
                }
            });
        }
    }
}
