package io.wispforest.owo.mixin;

import io.wispforest.owo.util.pond.MatrixStackManipulator;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin implements MatrixStackManipulator<DrawContext> {

    @Override
    public DrawContext getContext() {
        return (DrawContext) (Object) this;
    }
}
