package io.wispforest.owo.ui.layers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.pond.OwoScreenExtension;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class Layers {

    public static Identifier INIT_PHASE = new Identifier("owo", "init-layers");

    private static final Multimap<Class<? extends Screen>, Layer<?, ?>> LAYERS = HashMultimap.create();

    public static <S extends Screen, R extends ParentComponent> Layer<S, R> push(Class<S> screenClass, BiFunction<Sizing, Sizing, R> rootComponentMaker, Consumer<Layer<S, R>.Instance> instanceInitializer) {
        final var layer = new Layer<>(rootComponentMaker, instanceInitializer);
        LAYERS.put(screenClass, layer);
        return layer;
    }

    @SuppressWarnings("unchecked")
    public static <S extends Screen> Collection<Layer<S, ?>> getLayers(Class<S> screenClass) {
        return (Collection<Layer<S, ?>>) (Object) LAYERS.get(screenClass);
    }

    @SuppressWarnings("unchecked")
    public static <S extends Screen> List<Layer<S, ?>.Instance> getInstances(S screen) {
        return (List<Layer<S, ?>.Instance>) (Object) ((OwoScreenExtension) screen).owo$getLayersView();
    }

    static {
        ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, INIT_PHASE);
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ((OwoScreenExtension) screen).owo$updateLayers();

            ScreenEvents.beforeRender(screen).register((bruh, matrices, mouseX, mouseY, tickDelta) -> {
                for (var instance : getInstances(screen)) {
                    if (instance.aggressivePositioning) instance.dispatchLayoutUpdates();
                }
            });

            ScreenEvents.afterRender(screen).register((bruh, matrices, mouseX, mouseY, tickDelta) -> {
                for (var instance : getInstances(screen)) {
                    instance.adapter.render(matrices, mouseX, mouseY, tickDelta);
                }
            });

            ScreenMouseEvents.allowMouseClick(screen).register((bruh, mouseX, mouseY, button) -> {
                boolean handled;
                for (var instance : getInstances(screen)) {
                    handled = instance.adapter.mouseClicked(mouseX, mouseY, button);
                    if (handled) return false;
                }

                return true;
            });

            ScreenMouseEvents.allowMouseRelease(screen).register((bruh, mouseX, mouseY, button) -> {
                boolean handled;
                for (var instance : getInstances(screen)) {
                    handled = instance.adapter.mouseReleased(mouseX, mouseY, button);
                    if (handled) return false;
                }

                return true;
            });

            ScreenMouseEvents.allowMouseScroll(screen).register((bruh, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
                boolean handled;
                for (var instance : getInstances(screen)) {
                    handled = instance.adapter.mouseScrolled(mouseX, mouseY, verticalAmount);
                    if (handled) return false;
                }

                return true;
            });

            ScreenKeyboardEvents.allowKeyPress(screen).register((bruh, key, scancode, modifiers) -> {
                boolean handled;
                for (var instance : getInstances(screen)) {
                    handled = instance.adapter.keyPressed(key, scancode, modifiers);
                    if (handled) return false;
                }

                return true;
            });

            ScreenKeyboardEvents.allowKeyRelease(screen).register((bruh, key, scancode, modifiers) -> {
                boolean handled;
                for (var instance : getInstances(screen)) {
                    handled = instance.adapter.keyReleased(key, scancode, modifiers);
                    if (handled) return false;
                }

                return true;
            });
        });
    }

}
