package io.wispforest.owo.ui.hud;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.event.ClientRenderCallback;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A utility for displaying owo-ui components on the
 * in-game HUD - rendered during {@link HudRenderCallback}
 */
public class Hud {

    static @Nullable OwoUIAdapter<FlowLayout> adapter = null;
    static boolean suppress = false;

    private static final Map<Identifier, Component> activeComponents = new HashMap<>();
    private static final List<Consumer<FlowLayout>> pendingActions = new ArrayList<>();

    /**
     * Add a new component to be rendered on the in-game HUD.
     * The root container used by the HUD does not support layout
     * positioning - the component supplied by {@code component}
     * must be explicitly positioned via either {@link io.wispforest.owo.ui.core.Positioning#absolute(int, int)}
     * or {@link io.wispforest.owo.ui.core.Positioning#relative(int, int)}
     *
     * @param id        An ID uniquely describing this HUD component
     * @param component A function creating the component
     *                  when the HUD is first rendered
     */
    public static void add(Identifier id, Supplier<Component> component) {
        pendingActions.add(flowLayout -> {
            var instance = component.get();

            flowLayout.child(instance);
            activeComponents.put(id, instance);
        });
    }

    /**
     * Remove the HUD component described by the given ID
     *
     * @param id The ID of the HUD component to remove
     */
    public static void remove(Identifier id) {
        pendingActions.add(flowLayout -> {
            var component = activeComponents.get(id);
            if (component == null) return;

            flowLayout.removeChild(component);
            activeComponents.remove(id);
        });
    }

    /**
     * Get the HUD component described by the given ID
     *
     * @param id The ID of the HUD component to query
     * @return The relevant HUD component, or {@code null} if there is none
     */
    public static @Nullable Component getComponent(Identifier id) {
        return activeComponents.get(id);
    }

    /**
     * @return {@code true} if there is an active HUD component described by {@code id}
     */
    public static boolean hasComponent(Identifier id) {
        return activeComponents.containsKey(id);
    }

    private static void initializeAdapter() {
        var window = MinecraftClient.getInstance().getWindow();
        adapter = OwoUIAdapter.createWithoutScreen(
            0, 0, window.getScaledWidth(), window.getScaledHeight(), HudContainer::new
        );

        adapter.inflateAndMount();
    }

    static {
        WindowResizeCallback.EVENT.register((client, window) -> {
            if (adapter == null) return;
            adapter.moveAndResize(0, 0, window.getScaledWidth(), window.getScaledHeight());
        });

        ClientRenderCallback.BEFORE.register(client -> {
            if (client.world == null) return;
            if (!pendingActions.isEmpty()) {
                if (adapter == null) initializeAdapter();

                pendingActions.forEach(action -> action.accept(adapter.rootComponent));
                pendingActions.clear();
            }
        });

        NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Post event) -> {
            var context = event.getGuiGraphics();
            var tickDelta = event.getPartialTick();

            if (adapter == null || suppress || MinecraftClient.getInstance().options.hudHidden) return;

            context.push().translate(0, 0, 100);
            adapter.render(context, -69, -69, tickDelta.getTickDelta(false));
            context.pop();
        });
    }

}
