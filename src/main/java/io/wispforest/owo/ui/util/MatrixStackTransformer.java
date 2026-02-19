package io.wispforest.owo.ui.util;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

/**
 * Helper interface implemented on top of the {@link GuiGraphics} to allow for easier matrix stack transformations
 */
public interface MatrixStackTransformer {

    default MatrixStackTransformer translate(double x, double y) {
        this.getMatrixStack().translate((float) x, (float) y);
        return this;
    }

    default MatrixStackTransformer translate(float x, float y) {
        this.getMatrixStack().translate(x, y);
        return this;
    }

    default MatrixStackTransformer scale(float x, float y) {
        this.getMatrixStack().scale(x, y);
        return this;
    }

    default MatrixStackTransformer push() {
        this.getMatrixStack().pushMatrix();
        return this;
    }

    default MatrixStackTransformer pop() {
        this.getMatrixStack().popMatrix();
        return this;
    }

    default MatrixStackTransformer mul(Matrix3x2f matrix) {
        this.getMatrixStack().mul(matrix);
        return this;
    }

    default Matrix3x2fStack getMatrixStack(){
        throw new IllegalStateException("getMatrices() method hasn't been override leading to exception!");
    }
}
