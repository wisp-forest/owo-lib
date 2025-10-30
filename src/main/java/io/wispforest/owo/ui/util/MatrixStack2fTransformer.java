package io.wispforest.owo.ui.util;

import io.wispforest.owo.ui.core.PositionedRectangle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.*;

import java.util.function.Consumer;

/**
 * Helper interface implemented on top of the {@link DrawContext} to allow for easier matrix stack transformations
 */
public interface MatrixStack2fTransformer<T extends MatrixStack2fTransformer<T>> {

    default T drawWithScissor(int x, int y, int width, int height, Consumer<T> consumer) {
        pushScissor(x, y, width, height);

        var t = this.owo$cast();

        consumer.accept(t);

        popScissor();

        return t;
    }

    default T pushScissor(PositionedRectangle rectangle) {
        return pushScissor(rectangle.x(), rectangle.y(), rectangle.width(), rectangle.height());
    }

    default T pushScissor(int x, int y, int width, int height) {
        throw new IllegalStateException("pushScissor() method hasn't been override leading to exception!");
    }

    default T popScissor() {
        throw new IllegalStateException("popScissor() method hasn't been override leading to exception!");
    }

    default T translate(Vector2f vec) {
        return translate(vec.x(), vec.y());
    }

    default T translate(double x, double y) {
        this.getMatrixStack().translate((float) x, (float) y);
        return owo$cast();
    }

    default T translate(float x, float y) {
        this.getMatrixStack().translate(x, y);
        return owo$cast();
    }

    default T scale(Vector2f vec) {
        return scale(vec.x(), vec.y());
    }

    default T scale(float x, float y) {
        this.getMatrixStack().scale(x, y);
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

    default T mul(Matrix3x2f matrix) {
        this.getMatrixStack().mul(matrix);
        return owo$cast();
    }

    default T applyStackTransformer(MatrixStack2fTransformer<?> transformer) {
        return applyStack(transformer.getMatrixStack());
    }

    default T applyStack(Matrix3x2fStack stack) {
        return applyStackEntry(stack);
    }

    default T applyStackEntry(Matrix3x2f entry) {
        var currentEntry = this.getMatrixStack();

        currentEntry.set(entry);

        return owo$cast();
    }

    default T owo$cast() {
        return (T) this;
    }

    default Matrix3x2fStack getMatrixStack(){
        throw new IllegalStateException("getMatrices() method hasn't been override leading to exception!");
    }
}
