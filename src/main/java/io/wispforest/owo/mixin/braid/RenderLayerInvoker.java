package io.wispforest.owo.mixin.braid;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import org.spongepowered.asm.mixin.gen.Invoker;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.render.RenderLayer.class)
public interface RenderLayerInvoker {
    @Invoker("of")
    static RenderLayer owo$of(String name, RenderSetup renderSetup) {throw new UnsupportedOperationException();}
}
