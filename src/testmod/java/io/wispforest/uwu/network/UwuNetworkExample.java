package io.wispforest.uwu.network;

import io.wispforest.endec.impl.RecordEndec;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class UwuNetworkExample {
    public static final Map<String, StructEndec<? extends DispatchedInterface>> REGISTRY = new HashMap<>();
    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(Identifier.fromNamespaceAndPath("uwu", "main"));

    public static void init() {
        CHANNEL.addEndecs(builder -> {
            builder.register(Endec.dispatchedStruct(REGISTRY::get, DispatchedInterface::getName, Endec.STRING), DispatchedInterface.class);
        });

        REGISTRY.put("one", RecordEndec.create(CHANNEL.builder(), DispatchedSubclassOne.class));
        REGISTRY.put("two", RecordEndec.create(CHANNEL.builder(), DispatchedSubclassTwo.class));

        CHANNEL.registerClientbound(StringPacket.class, (message, access) -> {
            access.player().displayClientMessage(Component.nullToEmpty(message.value()), false);
        });

        CHANNEL.registerServerbound(KeycodePacket.class, (message, access) -> {
            CHANNEL.serverHandle(access.player()).send(new StringPacket("Key " + message.key() + " pressed"));
        });

        CHANNEL.registerServerbound(MaldingPacket.class, (message, access) -> {
            access.player().displayClientMessage(Component.nullToEmpty(message.toString()), false);
        });

        CHANNEL.registerServerbound(NullablePacket.class, (message, access) -> {
            if(message.name() == null && message.names() == null) {
                access.player().sendSystemMessage(Component.nullToEmpty("NULLABLITY FOR THE WIN"));
            } else {
                var text = Component.literal("");

                text.append(Component.nullToEmpty(String.valueOf(message.name())));
                text.append(Component.nullToEmpty(String.valueOf(message.names())));

                access.player().sendSystemMessage(text);
            }
        });
    }

    @Environment(EnvType.CLIENT)
    public static final class Client {
        public static final KeyMapping NETWORK_TEST = new KeyMapping("key.uwu.network_test", GLFW.GLFW_KEY_U, KeyMapping.Category.MISC);

        public static void init() {
            KeyBindingHelper.registerKeyBinding(NETWORK_TEST);
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                while (NETWORK_TEST.consumeClick()) {
                    CHANNEL.clientHandle().send(new KeycodePacket(KeyBindingHelper.getBoundKeyOf(NETWORK_TEST).getValue()));

                    CHANNEL.clientHandle().send(new MaldingPacket(new DispatchedSubclassOne("base")));
                    CHANNEL.clientHandle().send(new MaldingPacket(new DispatchedSubclassTwo(20)));

                    CHANNEL.clientHandle().send(new NullablePacket(null, null));
                    CHANNEL.clientHandle().send(new NullablePacket("Weeee", null));
                }
            });
        }
    }
}
