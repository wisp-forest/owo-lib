package io.wispforest.owo.mixin;

import io.wispforest.owo.util.pond.MatrixStackTransformer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin implements MatrixStackTransformer {

    @Shadow public abstract MatrixStack getMatrices();

    @Override
    public MatrixStack getMatrixStack() {
        return getMatrices();
    }
}
