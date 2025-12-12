package io.wispforest.owo.braid.core;

import io.wispforest.owo.ui.event.ClientRenderCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class BraidWindowScheduler {

    private static final List<App> APPS = new ArrayList<>();

    public static void add(BraidWindow window, AppState app) {
        APPS.add(new App(window, app));
    }

    private static void frame() {
        for (var app : new ArrayList<>(APPS)) {
            if (!app.state().running()) {
                app.state().dispose();

                APPS.remove(app);
                continue;
            }

            app.state().processEvents(
                Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks()
            );

            app.state().draw(app.surface().guiRenderer.newGraphics(app.state().cursorPosition().x(), app.state().cursorPosition().y()));
        }
    }

    static {
        ClientRenderCallback.BEFORE_SWAP.register(client -> frame());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            APPS.forEach(app -> app.state().dispose());
            APPS.clear();
        });
    }
}

record App(BraidWindow surface, AppState state) {}
