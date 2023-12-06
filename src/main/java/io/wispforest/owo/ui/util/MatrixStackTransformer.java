package io.wispforest.owo.ui.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Helper interface implemented on top of the {@link DrawContext} to allow for easier matrix stack transformations
 */
public interface MatrixStackTransformer {

    default MatrixStackTransformer translate(double x, double y, double z) {
        this.getMatrixStack().translate(x, y, z);
        return this;
    }

    default MatrixStackTransformer translate(float x, float y, float z) {
        this.getMatrixStack().translate(x, y, z);
        return this;
    }

    default MatrixStackTransformer scale(float x, float y, float z) {
        this.getMatrixStack().scale(x, y, z);
        return this;
    }

    default MatrixStackTransformer multiply(Quaternionf quaternion) {
        this.getMatrixStack().multiply(quaternion);
        return this;
    }

    default MatrixStackTransformer multiply(Quaternionf quaternion, float originX, float originY, float originZ) {
        this.getMatrixStack().multiply(quaternion, originX, originY, originZ);
        return this;
    }

    default MatrixStackTransformer push() {
        this.getMatrixStack().push();
        return this;
    }

    default MatrixStackTransformer pop() {
        this.getMatrixStack().pop();
        return this;
    }

    default MatrixStackTransformer multiplyPositionMatrix(Matrix4f matrix) {
        this.getMatrixStack().multiplyPositionMatrix(matrix);
        return this;
    }

    default MatrixStack getMatrixStack(){
        throw new IllegalStateException("getMatrices() method hasn't been override leading to exception!");
    }
}
