package io.wispforest.owo.ui.layers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.screen.Screen;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class Layers {

    private static final Multimap<Class<? extends Screen>, Layer<?, ?>> LAYERS = HashMultimap.create();

    public static <S extends Screen, R extends ParentComponent> Layer<S, R> push(Class<S> screenClass, BiFunction<Sizing, Sizing, R> rootComponentMaker, BiConsumer<OwoUIAdapter<R>, Layer.Positioner> instanceInitializer) {
        final var layer = new Layer<S, R>(rootComponentMaker, instanceInitializer);
        LAYERS.put(screenClass, layer);
        return layer;
    }

    @SuppressWarnings("unchecked")
    public static <S extends Screen> Collection<Layer<S, ?>> getLayers(Class<S> screenClass) {
        return (Collection<Layer<S, ?>>) (Object) LAYERS.get(screenClass);
    }

}
