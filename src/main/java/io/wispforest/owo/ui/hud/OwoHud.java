package io.wispforest.owo.ui.hud;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

import javax.annotation.Nullable;

public class OwoHud {

    private static @Nullable OwoUIAdapter<FlowLayout> adapter = null;

    public static void add(Component component) {
        if (adapter == null) initializeAdapter();
        adapter.rootComponent.child(component);
    }

    public static void remove(Component component) {
        if (adapter == null) return;
        adapter.rootComponent.removeChild(component);
    }

    public static void onResized() {
        if (adapter == null) return;
        initializeAdapter();
    }

    private static void initializeAdapter() {
        var window = MinecraftClient.getInstance().getWindow();
        adapter = OwoUIAdapter.createWithoutScreen(
                0, 0, window.getScaledWidth(), window.getScaledHeight(),
                adapter == null ? HudContainer::new : (sizing, sizing2) -> adapter.rootComponent
        );

        adapter.inflateAndMount();
    }

    static {
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            if (adapter == null) return;
            adapter.render(matrixStack, -69, -69, tickDelta);
        });
    }

}
