package io.wispforest.owo.mixin.braid;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.gen.Invoker;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.renderer.rendertype.RenderType.class)
public interface RenderTypeInvoker {
    @Invoker("create")
    static RenderType owo$of(String name, RenderSetup renderSetup) {throw new UnsupportedOperationException();}
}
