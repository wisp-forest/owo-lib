package io.wispforest.owo.mixin.ui.layers;

import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.layers.Layer;
import io.wispforest.owo.ui.layers.Layer.Instance;
import io.wispforest.owo.ui.layers.Layers;
import io.wispforest.owo.util.pond.OwoScreenExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;

@Mixin(value = Screen.class, priority = 1100)
public abstract class ScreenMixin extends AbstractContainerEventHandler implements OwoScreenExtension {

    @Shadow public int width;
    @Shadow public int height;

    private final List<Layer<?, ?>.Instance> owo$instances = new ArrayList<>();
    private final List<Layer<?, ?>.Instance> owo$instancesView = Collections.unmodifiableList(this.owo$instances);
    private final Map<Layer<?, ?>, Layer<?, ?>.Instance> owo$layersToInstances = new HashMap<>();

    private boolean owo$layersInitialized = false;

    @SuppressWarnings("ConstantConditions")
    private Screen owo$this() {
        return (Screen) (Object) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void owo$updateLayers() {
        if (this.owo$layersInitialized) {
            for (var instance : this.owo$instances) {
                instance.resize(this.width, this.height);
            }
        } else {
            for (var layer : Layers.getLayers((Class<Screen>) this.owo$this().getClass())) {
                var instance = layer.instantiate(this.owo$this());
                this.owo$instances.add(instance);
                this.owo$layersToInstances.put(layer, instance);

                instance.adapter.inflateAndMount();
            }

            this.owo$layersInitialized = true;
        }

        this.owo$instances.forEach(Layer.Instance::dispatchLayoutUpdates);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends Screen, R extends ParentComponent> Layer<S, R>.Instance owo$getInstance(Layer<S, R> layer) {
        return (Layer<S, R>.Instance) this.owo$layersToInstances.get(layer);
    }

    @Override
    public List<Layer<?, ?>.Instance> owo$getInstancesView() {
        return this.owo$instancesView;
    }
}
