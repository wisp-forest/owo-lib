package io.wispforest.owo.util.pond;

import io.wispforest.owo.ui.layers.Layer;

import java.util.List;

public interface OwoScreenExtension {
    List<Layer<?, ?>.Instance> owo$getLayers();
    List<Layer<?, ?>.Instance> owo$getLayersView();

    void owo$updateLayers();
}
