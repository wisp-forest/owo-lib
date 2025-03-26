package io.wispforest.owo.ui.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.function.Consumer;

/**
 * Helper interface implemented on top of the {@link DrawContext} to allow for easier matrix stack transformations
 */
public interface MatrixStackTransformer<T extends MatrixStackTransformer<T>> {

    default T drawWithScissor(int x, int y, int width, int height, Consumer<T> consumer) {
        pushScissor(x, y, width, height);

        var t = this.owo$cast();

        consumer.accept(t);

        popScissor();

        return t;
    }

    default T pushScissor(int x, int y, int width, int height) {
        throw new IllegalStateException("pushScissor() method hasn't been override leading to exception!");
    }

    default T popScissor() {
        throw new IllegalStateException("popScissor() method hasn't been override leading to exception!");
    }

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
        this.getMatrixStack().pushMatrix();
        return owo$cast();
    }

    default T pop() {
        this.getMatrixStack().popMatrix();
        return owo$cast();
    }

    default T multiplyPositionMatrix(Matrix4f matrix) {
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

    default Matrix3x2fStack getMatrixStack(){
        throw new IllegalStateException("getMatrices() method hasn't been override leading to exception!");
    }
}
