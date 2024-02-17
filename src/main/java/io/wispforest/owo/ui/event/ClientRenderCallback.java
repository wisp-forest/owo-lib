package io.wispforest.owo.ui.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;

public interface ClientRenderCallback {

    /**
     * Invoked just before the client's window enters the 'Render' phase, after the client
     * has ticked and cleared the render task queue
     */
    Event<ClientRenderCallback> BEFORE = EventFactory.createArrayBacked(ClientRenderCallback.class, callbacks -> (client) -> {
        for (var callback : callbacks) {
            callback.onRender(client);
        }
    });

    /**
     * Called just after the client has finished rendering and drawing the
     * current frame and swapped buffers
     */
    Event<ClientRenderCallback> AFTER = EventFactory.createArrayBacked(ClientRenderCallback.class, callbacks -> (client) -> {
        for (var callback : callbacks) {
            callback.onRender(client);
        }
    });

    void onRender(MinecraftClient client);
}
