package io.wispforest.owo.extras;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;

public final class ServerConfigurationConnectionEvents {

//    public static final Event<Configure> BEFORE_CONFIGURE = EventFactory.createArrayBacked(Configure.class, callbacks -> (handler, server) -> {
//        for (Configure callback : callbacks) {
//            callback.onSendConfiguration(handler, server);
//        }
//    });

    public static final Event<Configure> CONFIGURE = EventFactory.createArrayBacked(Configure.class, callbacks -> (handler, server) -> {
        for (Configure callback : callbacks) {
            callback.onSendConfiguration(handler, server);
        }
    });

//    public static final Event<ServerConfigurationConnectionEvents.Disconnect> DISCONNECT = EventFactory.createArrayBacked(ServerConfigurationConnectionEvents.Disconnect.class, callbacks -> (handler, server) -> {
//        for (ServerConfigurationConnectionEvents.Disconnect callback : callbacks) {
//            callback.onConfigureDisconnect(handler, server);
//        }
//    });

    private ServerConfigurationConnectionEvents() {}

    @FunctionalInterface
    public interface Configure {
        void onSendConfiguration(ServerConfigurationNetworkHandler handler, MinecraftServer server);
    }

//    @FunctionalInterface
//    public interface Disconnect {
//        void onConfigureDisconnect(ServerConfigurationNetworkHandler handler, MinecraftServer server);
//    }
}
