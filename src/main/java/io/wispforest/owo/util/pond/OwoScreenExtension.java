package io.wispforest.owo.util.pond;

import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.util.layers.BraidLayersBinding;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.layers.Layer;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface OwoScreenExtension {
    List<Layer<?, ?>.Instance> owo$getInstancesView();
    <S extends Screen, R extends ParentUIComponent> Layer<S, R>.Instance owo$getInstance(Layer<S, R> layer);

    void owo$updateLayers();

    // ---

    void owo$setBraidLayersState(BraidLayersBinding.LayersState state);
    @Nullable BraidLayersBinding.LayersState owo$getBraidLayersState();
    default @Nullable AppState owo$getBraidLayersApp() {
        var state = this.owo$getBraidLayersState();
        if (state == null) {
            return null;
        }

        return state.app();
    }

}
