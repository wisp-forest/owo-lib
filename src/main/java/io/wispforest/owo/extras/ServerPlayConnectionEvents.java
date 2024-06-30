package io.wispforest.owo.extras;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.Consumer;

public class ServerPlayConnectionEvents {

//    public static final Event<Init> INIT = EventFactory.createArrayBacked(Init.class, callbacks -> (handler, server) -> {
//        for (Init callback : callbacks) {
//            callback.onPlayInit(handler, server);
//        }
//    });

    public static final Event<Join> JOIN = EventFactory.createArrayBacked(Join.class, callbacks -> (handler, sender, server) -> {
        for (Join callback : callbacks) {
            callback.onPlayReady(handler, sender, server);
        }
    });

//    public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (handler, server) -> {
//        for (Disconnect callback : callbacks) {
//            callback.onPlayDisconnect(handler, server);
//        }
//    });

    private ServerPlayConnectionEvents() {
    }

//    @FunctionalInterface
//    public interface Init {
//        void onPlayInit(ServerPlayNetworkHandler handler, MinecraftServer server);
//    }

    @FunctionalInterface
    public interface Join {
        void onPlayReady(ServerPlayNetworkHandler handler, Consumer<CustomPayload> sender, MinecraftServer server);
    }

//    @FunctionalInterface
//    public interface Disconnect {
//        void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server);
//    }
}
