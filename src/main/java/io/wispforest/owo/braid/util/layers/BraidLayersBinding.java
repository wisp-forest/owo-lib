package io.wispforest.owo.braid.util.layers;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.EventBinding;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.Surface;
import io.wispforest.owo.braid.core.events.*;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.eventstream.BraidEventStream;
import io.wispforest.owo.braid.widgets.overlay.Overlay;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.util.pond.OwoScreenExtension;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BraidLayersBinding {

    public static void add(Predicate<Screen> screenPredicate, Widget widget) {
        LAYERS.add(new Layer(screenPredicate, widget));
    }

    // ---

    @ApiStatus.Internal
    public static boolean tryHandleEvent(Screen screen, UserEvent event) {
        var app = ((OwoScreenExtension) screen).owo$getBraidLayersApp();
        if (app == null) {
            return false;
        }

        var slot = app.eventBinding.add(event);
        app.processEvents(0);

        return slot.handled();
    }

    @ApiStatus.Internal
    public static void renderLayers(Screen screen, DrawContext context, double mouseX, double mouseY) {
        var state = ((OwoScreenExtension) screen).owo$getBraidLayersState();
        if (state == null) {
            return;
        }

        state.refreshEvents.sink().onEvent(Unit.INSTANCE);
        state.app.eventBinding.add(new MouseMoveEvent(mouseX, mouseY));

        state.app.processEvents(MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration());
        state.app.draw(context);
    }

    private static void setupLayers(Screen screen) {
        var widgets = LAYERS.stream().filter(layer -> layer.screenPredicate.test(screen)).map(Layer::widget).toList();
        if (widgets.isEmpty()) {
            return;
        }

        var refreshEvents = new BraidEventStream<Unit>();
        var app = new AppState(
            null,
            "BraidLayersBinding",
            MinecraftClient.getInstance(),
            new Surface.Default(),
            new EventBinding.Default(),
            new LayerContext(
                refreshEvents.source(),
                screen,
                new Overlay(
                    new Stack(widgets)
                )
            )
        );

        ((OwoScreenExtension) screen).owo$setBraidLayersState(new LayersState(app, refreshEvents));
    }

    // ---

    public static final Identifier INIT_PHASE = Owo.id("init-braid-layers");

    private static final List<Layer> LAYERS = new ArrayList<>();

    private record Layer(Predicate<Screen> screenPredicate, Widget widget) {}

    @ApiStatus.Internal
    public record LayersState(AppState app, BraidEventStream<Unit> refreshEvents) {}

    // ---

    static {
        ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, INIT_PHASE);
        ScreenEvents.AFTER_INIT.register(INIT_PHASE, (client, screeen, scaledWidth, scaledHeight) -> {
            if (((OwoScreenExtension)screeen).owo$getBraidLayersState() == null) {
                setupLayers(screeen);
            }

            ScreenEvents.remove(screeen).register(screen -> {
                var app = ((OwoScreenExtension) screen).owo$getBraidLayersApp();
                if (app != null) {
                    app.dispose();
                }
            });

            ScreenMouseEvents.allowMouseClick(screeen).register((screen, mouseX, mouseY, button) -> {
                return !tryHandleEvent(screen, new MouseButtonPressEvent(button, KeyModifiers.NONE));
            });

            ScreenMouseEvents.allowMouseRelease(screeen).register((screen, mouseX, mouseY, button) -> {
                return !tryHandleEvent(screen, new MouseButtonReleaseEvent(button, KeyModifiers.NONE));
            });

            ScreenMouseEvents.allowMouseScroll(screeen).register((screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
                return !tryHandleEvent(screen, new MouseScrollEvent(horizontalAmount, verticalAmount));
            });

            ScreenKeyboardEvents.allowKeyPress(screeen).register((screen, key, scancode, modifiers) -> {
                return !tryHandleEvent(screen, new KeyPressEvent(key, scancode, modifiers));
            });

            ScreenKeyboardEvents.allowKeyRelease(screeen).register((screen, key, scancode, modifiers) -> {
                return !tryHandleEvent(screen, new KeyReleaseEvent(key, scancode, modifiers));
            });
        });
    }
}
