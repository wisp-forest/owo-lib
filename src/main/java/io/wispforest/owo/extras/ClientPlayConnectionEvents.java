package io.wispforest.owo.extras;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.CustomPayload;

import java.util.function.Consumer;

public class ClientPlayConnectionEvents {

//    public static final Event<Init> INIT = EventFactory.createArrayBacked(Init.class, callbacks -> (handler, client) -> {
//        for (Init callback : callbacks) {
//            callback.onPlayInit(handler, client);
//        }
//    });
//
//    public static final Event<Join> JOIN = EventFactory.createArrayBacked(Join.class, callbacks -> (handler, sender, client) -> {
//        for (Join callback : callbacks) {
//            callback.onPlayReady(handler, sender, client);
//        }
//    });

    public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (handler, client) -> {
        for (Disconnect callback : callbacks) {
            callback.onPlayDisconnect(handler, client);
        }
    });

    private ClientPlayConnectionEvents() {}

//    @FunctionalInterface
//    public interface Init {
//        void onPlayInit(ClientPlayNetworkHandler handler, MinecraftClient client);
//    }
//
//    @FunctionalInterface
//    public interface Join {
//        void onPlayReady(ClientPlayNetworkHandler handler, Consumer<CustomPayload> sender, MinecraftClient client);
//    }

    @FunctionalInterface
    public interface Disconnect {
        void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client);
    }
}
