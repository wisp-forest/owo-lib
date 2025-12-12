package io.wispforest.owo.braid.util.layers;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.EventBinding;
import io.wispforest.owo.braid.core.Surface;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
    public static void renderLayers(Screen screen, GuiGraphics graphics, double mouseX, double mouseY) {
        var state = ((OwoScreenExtension) screen).owo$getBraidLayersState();
        if (state == null) {
            return;
        }

        state.refreshEvents.sink().onEvent(Unit.INSTANCE);
        state.app.eventBinding.add(new MouseMoveEvent(mouseX, mouseY));

        state.app.processEvents(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks());
        state.app.draw(graphics);

        var cursorStyle = ((LayerSurface) state.app.surface).currentCursorStyle;
        if (cursorStyle != CursorStyle.NONE && CURSOR_MAPPINGS.get().containsKey(cursorStyle)) {
            graphics.requestCursor(CURSOR_MAPPINGS.get().get(cursorStyle));
        }
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
            Minecraft.getInstance(),
            new LayerSurface(),
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

    private static class LayerSurface extends Surface.Default {

        public CursorStyle currentCursorStyle = CursorStyle.NONE;

        @Override
        public void setCursorStyle(CursorStyle style) {
            this.currentCursorStyle = style;
        }

        @Override
        public CursorStyle currentCursorStyle() {
            return this.currentCursorStyle;
        }
    }

    private static final Supplier<Map<CursorStyle, CursorType>> CURSOR_MAPPINGS = Suppliers.memoize(() -> Map.of(
        CursorStyle.POINTER, CursorTypes.ARROW,
        CursorStyle.TEXT, CursorTypes.IBEAM,
        CursorStyle.CROSSHAIR, CursorTypes.CROSSHAIR,
        CursorStyle.HAND, CursorTypes.POINTING_HAND,
        CursorStyle.VERTICAL_RESIZE, CursorTypes.RESIZE_NS,
        CursorStyle.HORIZONTAL_RESIZE, CursorTypes.RESIZE_EW,
        CursorStyle.MOVE, CursorTypes.RESIZE_ALL,
        CursorStyle.NOT_ALLOWED, CursorTypes.NOT_ALLOWED
    ));

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

            ScreenMouseEvents.allowMouseClick(screeen).register((screen, click) -> {
                return !tryHandleEvent(screen, new MouseButtonPressEvent(click.button(), click.modifiers()));
            });

            ScreenMouseEvents.allowMouseRelease(screeen).register((screen, click) -> {
                return !tryHandleEvent(screen, new MouseButtonReleaseEvent(click.button(), click.modifiers()));
            });

            ScreenMouseEvents.allowMouseScroll(screeen).register((screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
                return !tryHandleEvent(screen, new MouseScrollEvent(horizontalAmount, verticalAmount));
            });

            ScreenKeyboardEvents.allowKeyPress(screeen).register((screen, keyInput) -> {
                return !tryHandleEvent(screen, new KeyPressEvent(keyInput.key(), keyInput.scancode(), keyInput.modifiers()));
            });

            ScreenKeyboardEvents.allowKeyRelease(screeen).register((screen, keyInput) -> {
                return !tryHandleEvent(screen, new KeyReleaseEvent(keyInput.key(), keyInput.scancode(), keyInput.modifiers()));
            });
        });
    }
}
