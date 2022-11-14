package io.wispforest.owo.mixin.ui.layers;

import io.wispforest.owo.ui.layers.Layer;
import io.wispforest.owo.ui.layers.Layers;
import io.wispforest.owo.util.pond.OwoScreenExtension;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(value = Screen.class, priority = 1100)
public abstract class ScreenMixin extends AbstractParentElement implements OwoScreenExtension {

    @Shadow public int width;
    @Shadow public int height;

    private final List<Layer<?, ?>.Instance> owo$layers = new ArrayList<>();
    private final List<Layer<?, ?>.Instance> owo$layersView = Collections.unmodifiableList(this.owo$layers);
    private boolean owo$layersInitialized = false;

    @SuppressWarnings("ConstantConditions")
    private Screen owo$this() {
        return (Screen) (Object) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void owo$updateLayers() {
        if (this.owo$layersInitialized) {
            for (var instance : this.owo$layers) {
                instance.resize(this.width, this.height);
            }
        } else {
            for (var layer : Layers.getLayers((Class<Screen>) this.owo$this().getClass())) {
                var instance = layer.instantiate(this.owo$this());
                this.owo$layers.add(instance);

                instance.adapter.inflateAndMount();
            }

            this.owo$layersInitialized = true;
        }

        this.owo$layers.forEach(Layer.Instance::dispatchLayoutUpdates);
    }

    @Override
    public List<Layer<?, ?>.Instance> owo$getLayers() {
        return this.owo$layers;
    }

    @Override
    public List<Layer<?, ?>.Instance> owo$getLayersView() {
        return this.owo$layersView;
    }
}
