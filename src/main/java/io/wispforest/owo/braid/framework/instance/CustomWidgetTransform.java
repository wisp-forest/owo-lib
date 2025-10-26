package io.wispforest.owo.braid.framework.instance;

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
                this.toParent = new Matrix3x2f()
                    .translate((float) this.x, (float) this.y)
                    .mul(this.matrix);
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
    public void toParentCoordinates(Vector2d vec) {
        var vec2f = new Vector2f(vec);
        this.toParent().transformPosition(vec2f);

        vec.set(vec2f.x, vec2f.y);
    }

    @Override
    public void toWidgetCoordinates(Vector2d vec) {
        var vec2f = new Vector2f(vec);
        this.toWidget().transformPosition(vec2f);

        vec.set(vec2f.x, vec2f.y);
    }

    @Override
    public void recompute() {
        super.recompute();
        this.toParent = null;
        this.toWidget = null;
    }
}
