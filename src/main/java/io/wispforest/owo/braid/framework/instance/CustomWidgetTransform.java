package io.wispforest.owo.braid.framework.instance;

import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3d;

public class CustomWidgetTransform extends WidgetTransform {

    protected @Nullable Matrix4f toParent;
    protected @Nullable Matrix4f toWidget;

    private boolean applyAtCenter = true;
    private Matrix4f matrix = new Matrix4f();

    public void setMatrix(Matrix4f matrix) {
        this.setState(() -> this.matrix = matrix);
    }

    public Matrix4f matrix() {
        return this.matrix;
    }

    public void setApplyAtCenter(boolean applyToCenter) {
        this.setState(() -> this.applyAtCenter = applyToCenter);
    }

    public boolean applyAtCenter() {
        return this.applyAtCenter;
    }

    protected Matrix4fc toParent() {
        if (this.toParent == null) {
            if (this.applyAtCenter) {
                this.toParent = new Matrix4f()
                    .translate((float) (this.x + this.width / 2), (float) (this.y + this.height / 2), 0)
                    .mul(this.matrix)
                    .translate((float) (-this.width / 2), (float) (-this.height / 2), 0);
            } else {
                this.toParent = new Matrix4f()
                    .translate((float) this.x, (float) this.y, 0)
                    .mul(this.matrix);
            }
        }

        return this.toParent;
    }

    protected Matrix4fc toWidget() {
        if (this.toWidget == null) {
            this.toWidget = new Matrix4f(this.toParent()).invert();
        }

        return this.toWidget;
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
    public void toParentCoordinates(Vector3d vec) {
        vec.mulPosition(this.toParent());
    }

    @Override
    public void toWidgetCoordinates(Vector3d vec) {
        vec.mulPosition(this.toWidget());
    }

    @Override
    public void recompute() {
        super.recompute();
        this.toParent = null;
        this.toWidget = null;
    }
}
