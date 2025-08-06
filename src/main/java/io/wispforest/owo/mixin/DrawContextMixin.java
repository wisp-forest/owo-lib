package io.wispforest.owo.mixin;

import io.wispforest.owo.ui.util.MatrixStackTransformer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin implements MatrixStackTransformer {

    @Shadow public abstract Matrix3x2fStack getMatrices();

    @Override
    public Matrix3x2fStack getMatrixStack() {
        return this.getMatrices();
    }
}
