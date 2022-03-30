package io.wispforest.uwu.network;

import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class UwuNetworkExample {
    public static final Map<String, PacketBufSerializer<? extends DispatchedInterface>> REGISTRY = new HashMap<>();
    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(new Identifier("uwu", "main"));

    public static void init() {
        REGISTRY.put("one", PacketBufSerializer.get(DispatchedSubclassOne.class));
        REGISTRY.put("two", PacketBufSerializer.get(DispatchedSubclassTwo.class));

        PacketBufSerializer.register(DispatchedInterface.class, PacketBufSerializer.createDispatchedSerializer(REGISTRY::get, DispatchedInterface::getName, PacketBufSerializer.get(String.class)));

        CHANNEL.registerClientbound(StringPacket.class, (message, access) -> {
            access.player().sendMessage(Text.of(message.value()), false);
        });

        CHANNEL.registerServerbound(KeycodePacket.class, (message, access) -> {
            CHANNEL.serverHandle(access.player()).send(new StringPacket("Key " + message.key() + " pressed"));
        });

        CHANNEL.registerServerbound(MaldingPacket.class, (message, access) -> {
            access.player().sendMessage(Text.of(message.toString()), false);
        });
    }

    @Environment(EnvType.CLIENT)
    public static final class Client {
        public static final KeyBinding NETWORK_TEST = new KeyBinding("key.uwu.network_test", GLFW.GLFW_KEY_U, "misc");

        public static void init() {
            KeyBindingHelper.registerKeyBinding(NETWORK_TEST);
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                while (NETWORK_TEST.wasPressed()) {
                    CHANNEL.clientHandle().send(new KeycodePacket(KeyBindingHelper.getBoundKeyOf(NETWORK_TEST).getCode()));

                    CHANNEL.clientHandle().send(new MaldingPacket(new DispatchedSubclassOne("base")));
                    CHANNEL.clientHandle().send(new MaldingPacket(new DispatchedSubclassTwo(20)));
                }
            });
        }
    }
}
