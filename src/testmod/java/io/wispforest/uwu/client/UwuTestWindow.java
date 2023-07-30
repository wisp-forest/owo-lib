package io.wispforest.uwu.client;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.window.OwoWindow;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class UwuTestWindow extends OwoWindow<FlowLayout> {
    public UwuTestWindow() {
        super(640, 480, "uωu test window!", MinecraftClient.getInstance().getWindow().getHandle());
    }

    public static void init() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            var window = new UwuTestWindow();

            ClientTickEvents.END_CLIENT_TICK.register(client1 -> {
                if (window.closed()) return;

                window.render();
            });
        });
    }

    @Override
    protected OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.createWithoutScreen(0, 0, scaledWidth(), scaledHeight(), Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.margins(Insets.of(10));
        rootComponent.child(Components.label(Text.literal("Honestly quite shrimple")));
        rootComponent.child(Components.button(Text.literal("Honestly quite shrimple"), unused -> {}));
    }
}
