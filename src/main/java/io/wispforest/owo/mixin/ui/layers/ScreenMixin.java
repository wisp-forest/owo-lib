package io.wispforest.owo.mixin.ui.layers;

import io.wispforest.owo.ui.layers.Layer;
import io.wispforest.owo.ui.layers.Layers;
import io.wispforest.owo.util.pond.OwoScreenExtension;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin extends AbstractParentElement implements OwoScreenExtension {

    @Shadow public int width;
    @Shadow public int height;

    private final List<Layer<?, ?>.Instance> owo$layers = new ArrayList<>();
    private boolean owo$layersInitialized = false;

    @SuppressWarnings("ConstantConditions")
    private Screen owo$this() {
        return (Screen) (Object) this;
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "clearAndInit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init()V", shift = At.Shift.AFTER))
    private void initializeLayers(CallbackInfo ci) {
        if (this.owo$layersInitialized) {
            for (var instance : this.owo$layers) {
                instance.resize(this.width, this.height);
            }
        } else {
            for (var layer : Layers.getLayers((Class<Screen>) this.owo$this().getClass())) {
                var instance = layer.instantiate(this.owo$this(), this.width, this.height);
                this.owo$layers.add(instance);

                instance.adapter.inflateAndMount();
            }

            this.owo$layersInitialized = true;
        }
    }

    @Override
    public List<Layer<?, ?>.Instance> owo$getLayers() {
        return this.owo$layers;
    }
}
