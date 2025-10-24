package io.wispforest.owo.util.pond;

import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.layers.Layer;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface OwoScreenExtension {
    List<Layer<?, ?>.Instance> owo$getInstancesView();
    <S extends Screen, R extends ParentComponent> Layer<S, R>.Instance owo$getInstance(Layer<S, R> layer);

    void owo$updateLayers();

    // ---

    void owo$setBraidLayersState(AppState state);
    @Nullable AppState owo$getBraidLayersState();
}
