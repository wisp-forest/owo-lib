package io.wispforest.owo.braid.framework.instance;

import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

public class CustomWidgetTransform extends WidgetTransform {

    protected @Nullable Matrix3x2f toParent;
    protected @Nullable Matrix3x2f toWidget;

    private boolean applyAtCenter = true;
    private Matrix3x2f matrix = new Matrix3x2f();

    public void setMatrix(Matrix3x2f matrix) {
        this.setState(() -> this.matrix = matrix);
    }

    public Matrix3x2f matrix() {
        return this.matrix;
    }

    public void setApplyAtCenter(boolean applyToCenter) {
        this.setState(() -> this.applyAtCenter = applyToCenter);
    }

    public boolean applyAtCenter() {
        return this.applyAtCenter;
    }

     protected Matrix3x2fc toParent() {
        if (this.toParent == null) {
            if (this.applyAtCenter) {
                this.toParent = new Matrix3x2f()
                    .translate((float) (this.x + this.width / 2), (float) (this.y + this.height / 2))
                    .mul(this.matrix)
                    .translate((float) (-this.width / 2), (float) (-this.height / 2));
            } else {
                this.toParent = new Matrix3x2f(this.matrix);
            }
        }

        return this.toParent;
    }

    protected Matrix3x2fc toWidget() {
        if (this.toWidget == null) {
            this.toWidget = new Matrix3x2f(this.toParent()).invert();
        }

        return this.toWidget;
    }

    @Override
    public void transformToParent(Matrix3x2f mat) {
        mat.mul(this.toParent());
    }

    @Override
    public void transformToParent(Matrix3x2fStack matrices) {
        matrices.mul(this.toParent());
    }

    @Override
    public void transformToWidget(Matrix3x2f mat) {
        mat.mul(this.toWidget());
    }

    @Override
    public void transformToWidget(Matrix3x2fStack matrices) {
        matrices.mul(this.toWidget());
    }

    @Override
    public void toParentCoordinates(Vector3f vec) {
        var vec2 = new Vector2f(vec.x, vec.y);
        vec2.mulPosition(this.toParent());

        vec.set(vec2.x, vec.y, vec.z);
    }

    @Override
    public void toWidgetCoordinates(Vector3f vec) {
        var vec2 = new Vector2f(vec.x, vec.y);
        vec2.mulPosition(this.toWidget());

        vec.set(vec2.x, vec.y, vec.z);
    }

    @Override
    public void recompute() {
        super.recompute();
        this.toParent = null;
        this.toWidget = null;
    }
}
