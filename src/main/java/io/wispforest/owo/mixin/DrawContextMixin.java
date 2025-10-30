package io.wispforest.owo.mixin;

import io.wispforest.owo.ui.util.MatrixStack2fTransformer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin implements MatrixStack2fTransformer<DrawContext> {

    @Shadow public abstract Matrix3x2fStack getMatrices();

    @Shadow @Final public DrawContext.ScissorStack scissorStack;

    @Shadow @Final private Matrix3x2fStack matrices;

    @Shadow public abstract void disableScissor();

    @Override
    public Matrix3x2fStack getMatrixStack() {
        return this.getMatrices();
    }

    @Override
    public DrawContext pushScissor(int x, int y, int width, int height) {
        var rect = new ScreenRect(x, y, width, height).transform(this.matrices);

        this.scissorStack.push(rect);

        return owo$cast();
    }

    @Override
    public DrawContext popScissor() {
        this.disableScissor();

        return owo$cast();
    }

    @Override
    public DrawContext owo$cast() {
        return (DrawContext)(Object) this;
    }
}
