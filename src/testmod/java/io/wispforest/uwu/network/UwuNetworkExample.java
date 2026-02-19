package io.wispforest.uwu.network;

import io.wispforest.endec.impl.RecordEndec;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
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

    //@OnlyIn(Dist.CLIENT)
    public static final class Client {
        public static final KeyMapping NETWORK_TEST = new KeyMapping("key.uwu.network_test", GLFW.GLFW_KEY_U, KeyMapping.Category.MISC);

        public static void init(IEventBus eventBus) {
            eventBus.addListener((RegisterKeyMappingsEvent mappingsEvent) -> {
                mappingsEvent.register(NETWORK_TEST);
            });

            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post clientEvent) -> {
                while (NETWORK_TEST.consumeClick()) {

                    CHANNEL.clientHandle().send(new KeycodePacket(NETWORK_TEST.getKey().getValue()));

                    CHANNEL.clientHandle().send(new MaldingPacket(new DispatchedSubclassOne("base")));
                    CHANNEL.clientHandle().send(new MaldingPacket(new DispatchedSubclassTwo(20)));

                    CHANNEL.clientHandle().send(new NullablePacket(null, null));
                    CHANNEL.clientHandle().send(new NullablePacket("Weeee", null));
                }
            });
        }
    }
}
