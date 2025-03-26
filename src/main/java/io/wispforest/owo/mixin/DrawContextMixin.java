package io.wispforest.owo.mixin;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.util.MatrixStackTransformer;
import io.wispforest.owo.ui.util.ScissorStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin implements MatrixStackTransformer<DrawContext> {

    @Shadow public abstract Matrix3x2fStack getMatrices();

    @Shadow public abstract void draw();

    @Override
    public Matrix3x2fStack getMatrixStack() {
        return this.getMatrices();
    }

    @Override
    public DrawContext pushScissor(int x, int y, int width, int height) {
        ScissorStack.push(x, y, width, height, (DrawContext) (Object) this);

        return owo$cast();
    }

    @Override
    public DrawContext popScissor() {
        this.draw();

        ScissorStack.pop();

        return owo$cast();
    }

    @Override
    public DrawContext owo$cast() {
        return (DrawContext)(Object) this;
    }
}
