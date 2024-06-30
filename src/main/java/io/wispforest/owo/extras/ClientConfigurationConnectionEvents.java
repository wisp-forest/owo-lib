package io.wispforest.owo.extras;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.packet.CustomPayload;

public final class ClientConfigurationConnectionEvents {

//    public static final Event<Init> INIT = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Init.class, callbacks -> (handler, client) -> {
//        for (ClientConfigurationConnectionEvents.Init callback : callbacks) {
//            callback.onConfigurationInit(handler, client);
//        }
//    });
//
//    public static final Event<ClientConfigurationConnectionEvents.Start> START = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Start.class, callbacks -> (handler, client) -> {
//        for (ClientConfigurationConnectionEvents.Start callback : callbacks) {
//            callback.onConfigurationStart(handler, client);
//        }
//    });

    public static final Event<ClientConfigurationConnectionEvents.Complete> COMPLETE = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Complete.class, callbacks -> (handler, client) -> {
        for (ClientConfigurationConnectionEvents.Complete callback : callbacks) {
            callback.onConfigurationComplete(handler, client);
        }
    });

    public static final Event<ClientConfigurationConnectionEvents.Disconnect> DISCONNECT = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Disconnect.class, callbacks -> (handler, client) -> {
        for (ClientConfigurationConnectionEvents.Disconnect callback : callbacks) {
            callback.onConfigurationDisconnect(handler, client);
        }
    });

    private ClientConfigurationConnectionEvents() {}

//    @FunctionalInterface
//    public interface Init {
//        void onConfigurationInit(ClientConfigurationNetworkHandler handler, MinecraftClient client);
//    }
//
//    @FunctionalInterface
//    public interface Start {
//        void onConfigurationStart(ClientConfigurationNetworkHandler handler, MinecraftClient client);
//    }

    @FunctionalInterface
    public interface Complete {
        void onConfigurationComplete(ClientConfigurationNetworkHandler handler, MinecraftClient client);
    }

    @FunctionalInterface
    public interface Disconnect {
        void onConfigurationDisconnect(ClientConfigurationNetworkHandler handler, MinecraftClient client);
    }
}
