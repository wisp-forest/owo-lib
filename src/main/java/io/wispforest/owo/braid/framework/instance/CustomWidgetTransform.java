package io.wispforest.owo.braid.framework.instance;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public class CustomWidgetTransform extends WidgetTransform {
    private Matrix4f matrix = new Matrix4f();

    public void setMatrix(Matrix4f matrix) {
        this.setState(() -> this.matrix = matrix);
    }

    public Matrix4f matrix() {
        return this.matrix;
    }

    @Override
    public Matrix4fc toParent() {
        if (this.toParent == null) {
            this.toParent = new Matrix4f()
                .translate((float) (this.x + this.width / 2), (float) (this.y + this.height / 2), 0)
                .mul(this.matrix)
                .translate((float) (-this.width / 2), (float) (-this.height / 2), 0);
        }

        return this.toParent;
    }

    @Override
    public void transformToParent(Matrix4f mat) {
        mat.mul(this.toParent());
    }

    @Override
    public void transformToParent(MatrixStack matrices) {
        matrices.peek().getPositionMatrix().mul(this.toParent());
    }

    @Override
    public void transformToWidget(Matrix4f mat) {
        mat.mul(this.toWidget());
    }

    @Override
    public void transformToWidget(MatrixStack matrices) {
        matrices.peek().getPositionMatrix().mul(this.toWidget());
    }

    @Override
    public void toParentCoordinates(Vector3f vec) {
        vec.mulPosition(this.toParent());
    }

    @Override
    public void toWidgetCoordinates(Vector3f vec) {
        vec.mulPosition(this.toWidget());
    }
}
