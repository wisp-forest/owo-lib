package io.wispforest.owo.ui.util;

import io.wispforest.owo.ui.core.PositionedRectangle;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.*;

public interface MatrixStackTransformer<T extends MatrixStackTransformer<T>> {

    default T translate(Vector3f vec) {
        return translate(vec.x(), vec.y(), vec.z());
    }

    default T translate(double x, double y, double z) {
        this.getMatrixStack().translate(x, y, z);
        return owo$cast();
    }

    default T translate(float x, float y, float z) {
        this.getMatrixStack().translate(x, y, z);
        return owo$cast();
    }

    default T scale(Vector3f vec) {
        return scale(vec.x(), vec.y(), vec.z());
    }

    default T scale(float x, float y, float z) {
        this.getMatrixStack().scale(x, y, z);
        return owo$cast();
    }

    default T multiply(Quaternionf quaternion) {
        this.getMatrixStack().multiply(quaternion);
        return owo$cast();
    }

    default T multiply(Quaternionf quaternion, Vector3f origin) {
        return multiply(quaternion, origin.x(), origin.y(), origin.z());
    }

    default T multiply(Quaternionf quaternion, float originX, float originY, float originZ) {
        this.getMatrixStack().multiply(quaternion, originX, originY, originZ);
        return owo$cast();
    }

    default T push() {
        this.getMatrixStack().push();
        return owo$cast();
    }

    default T pop() {
        this.getMatrixStack().pop();
        return owo$cast();
    }

    default T multiplyPositionMatrix(Matrix4fc matrix) {
        this.getMatrixStack().multiplyPositionMatrix(matrix);
        return owo$cast();
    }

    default T applyStackTransformer(MatrixStackTransformer<?> transformer) {
        return applyStack(transformer.getMatrixStack());
    }

    default T applyStack(MatrixStack stack) {
        return applyStackEntry(stack.peek());
    }

    default T applyStackEntry(MatrixStack.Entry entry) {
        var currentEntry = this.getMatrixStack().peek();

        currentEntry.getPositionMatrix().mul(entry.getPositionMatrix());
        currentEntry.getNormalMatrix().mul(entry.getNormalMatrix());

        return owo$cast();
    }

    default T owo$cast() {
        return (T) this;
    }

    default MatrixStack getMatrixStack(){
        throw new IllegalStateException("getMatrices() method hasn't been override leading to exception!");
    }
}
