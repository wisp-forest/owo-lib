package io.wispforest.owo.ui.layers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.pond.OwoScreenExtension;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * A system for adding owo-ui components onto existing screens.
 * <p>
 * You can create a new layer by calling {@link #add(BiFunction, Consumer, Class[])}. The
 * second argument to this function is the instance initializer, which is where you configure
 * instances of your layer added onto screens when they get initialized. This is the place to
 * configure the UI adapter of your layer as well as building your UI tree onto the root
 * component of said adapter
 * <p>
 * Just like proper owo-ui screens, layers preserve state when the client's window
 * is resized - they are only initialized once, when the screen is first opened
 */
public final class Layers {

    /**
     * The event phase during which owo-ui layer instances are created and
     * initialized. This runs after the default phase
     */
    public static final Identifier INIT_PHASE = Identifier.of("owo", "init-layers");

    private static final Multimap<Class<? extends Screen>, Layer<?, ?>> LAYERS = HashMultimap.create();

    /**
     * Add a new layer to the given screens
     *
     * @param rootComponentMaker  A function which will create the root component of this layer
     * @param instanceInitializer A function which will initialize any instances of this layer which get created.
     *                            This is where you add components or configure the UI adapter of the generated layer instance
     * @param screenClasses       The screens onto which to add the new layer
     */
    @SafeVarargs
    public static <S extends Screen, R extends ParentComponent> Layer<S, R> add(BiFunction<Sizing, Sizing, R> rootComponentMaker, Consumer<Layer<S, R>.Instance> instanceInitializer, Class<? extends S>... screenClasses) {
        final var layer = new Layer<>(rootComponentMaker, instanceInitializer);
        for (var screenClass : screenClasses) {
            LAYERS.put(screenClass, layer);
        }
        return layer;
    }

    /**
     * Get all layers associated with a given screen
     */
    @SuppressWarnings("unchecked")
    public static <S extends Screen> Collection<Layer<S, ?>> getLayers(Class<S> screenClass) {
        return (Collection<Layer<S, ?>>) (Object) LAYERS.get(screenClass);
    }

    /**
     * Get all layer instances currently present on the given screen
     */
    @SuppressWarnings("unchecked")
    public static <S extends Screen> List<Layer<S, ?>.Instance> getInstances(S screen) {
        return (List<Layer<S, ?>.Instance>) (Object) ((OwoScreenExtension) screen).owo$getInstancesView();
    }

    static {
        NeoForge.EVENT_BUS.<ScreenEvent.Init.Post>addListener(EventPriority.LOW, (event) -> {
            ((OwoScreenExtension) event.getScreen()).owo$updateLayers();
        });

        NeoForge.EVENT_BUS.<ScreenEvent.Closing>addListener(EventPriority.LOW, (event) -> {
            for (var instance : getInstances(event.getScreen())) {
                instance.adapter.dispose();
            }
        });

        NeoForge.EVENT_BUS.<ScreenEvent.Render.Pre>addListener(EventPriority.LOW, (event) -> {
            for (var instance : getInstances(event.getScreen())) {
                if (instance.aggressivePositioning) instance.dispatchLayoutUpdates();
            }
        });

        NeoForge.EVENT_BUS.<ScreenEvent.Render.Post>addListener(EventPriority.LOW, (event) -> {
            for (var instance : getInstances(event.getScreen())) {
                instance.adapter.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
            }

            for (var instance : getInstances(event.getScreen())) {
                instance.adapter.drawTooltip(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
            }
        });

        NeoForge.EVENT_BUS.<ScreenEvent.MouseButtonPressed.Pre>addListener(EventPriority.LOW, (event) -> {
            boolean handled;
            for (var instance : getInstances(event.getScreen())) {
                handled = instance.adapter.mouseClicked(event.getMouseButtonEvent(), false);
                if (handled) {
                    event.setCanceled(true);
                    return;
                }
            }
        });

        NeoForge.EVENT_BUS.<ScreenEvent.MouseButtonReleased.Pre>addListener(EventPriority.LOW, (event) -> {
            boolean handled;
            for (var instance : getInstances(event.getScreen())) {
                handled = instance.adapter.mouseReleased(event.getMouseButtonEvent());
                if (handled) {
                    event.setCanceled(true);
                    return;
                }
            }
        });

        NeoForge.EVENT_BUS.<ScreenEvent.MouseScrolled.Pre>addListener(EventPriority.LOW, (event) -> {
            boolean handled;
            for (var instance : getInstances(event.getScreen())) {
                handled = instance.adapter.mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDeltaX(), event.getScrollDeltaY());
                if (handled) {
                    event.setCanceled(true);
                    return;
                }
            }
        });

        NeoForge.EVENT_BUS.<ScreenEvent.KeyPressed.Pre>addListener(EventPriority.LOW, (event) -> {
            boolean handled;
            for (var instance : getInstances(event.getScreen())) {
                handled = instance.adapter.keyPressed(event.getKeyEvent());
                if (handled) {
                    event.setCanceled(true);
                    return;
                }
            }
        });

        NeoForge.EVENT_BUS.<ScreenEvent.KeyReleased.Pre>addListener(EventPriority.LOW, (event) -> {
            boolean handled;
            for (var instance : getInstances(event.getScreen())) {
                handled = instance.adapter.keyReleased(event.getKeyEvent());
                if (handled) {
                    event.setCanceled(true);
                    return;
                }
            }
        });
    }

}
