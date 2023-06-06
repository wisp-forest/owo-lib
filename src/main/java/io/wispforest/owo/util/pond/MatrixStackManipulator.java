package io.wispforest.owo.util.pond;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public interface MatrixStackManipulator<C extends DrawContext> {

    default C translate(double x, double y, double z) {
        C context = getContext();

        context.getMatrices().translate(x, y, z);

        return context;
    }

    default C translate(float x, float y, float z) {
        C context = getContext();

        context.getMatrices().translate(x, y, z);

        return context;
    }

    default C scale(float x, float y, float z) {
        C context = getContext();

        context.getMatrices().scale(x, y, z);

        return context;
    }

    default C multiply(Quaternionf quaternion) {
        C context = getContext();

        context.getMatrices().multiply(quaternion);

        return context;
    }

    default C multiply(Quaternionf quaternion, float originX, float originY, float originZ) {
        C context = getContext();

        context.getMatrices().multiply(quaternion, originX, originY, originZ);

        return context;
    }

    default C push() {
        C context = getContext();

        context.getMatrices().push();

        return context;
    }

    default C pop() {
        C context = getContext();

        context.getMatrices().pop();

        return context;
    }

    default C multiplyPositionMatrix(Matrix4f matrix) {
        C context = getContext();

        context.getMatrices().multiplyPositionMatrix(matrix);

        return context;
    }

    default C getContext(){
        throw new IllegalStateException("getContext() method hasn't been override leading to exception!");
    }
}
