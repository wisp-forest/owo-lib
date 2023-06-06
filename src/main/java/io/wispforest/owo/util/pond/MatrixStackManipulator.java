package io.wispforest.owo.util.pond;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Helper Interface implemented on top of the DrawContext through a Mixin allowing for easier matrix stack manipulation
 */
public interface MatrixStackManipulator<C extends MatrixStackManipulator<?>> {

    static <C extends MatrixStackManipulator<?>> C of(DrawContext c){
        return (C) c;
    }

    //----

    default C translate(double x, double y, double z) {
        getMatrices().translate(x, y, z);

        return (C) this;
    }

    default C translate(float x, float y, float z) {
        getMatrices().translate(x, y, z);

        return (C) this;
    }

    default C scale(float x, float y, float z) {
        getMatrices().scale(x, y, z);

        return (C) this;
    }

    default C multiply(Quaternionf quaternion) {
        getMatrices().multiply(quaternion);

        return (C) this;
    }

    default C multiply(Quaternionf quaternion, float originX, float originY, float originZ) {
        getMatrices().multiply(quaternion, originX, originY, originZ);

        return (C) this;
    }

    default C push() {
        getMatrices().push();

        return (C) this;
    }

    default C pop() {
        getMatrices().pop();

        return (C) this;
    }

    default C multiplyPositionMatrix(Matrix4f matrix) {
        getMatrices().multiplyPositionMatrix(matrix);

        return (C) this;
    }

    default MatrixStack getMatrices(){
        throw new IllegalStateException("getContext() method hasn't been override leading to exception!");
    }
}
