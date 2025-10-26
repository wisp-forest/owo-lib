package io.wispforest.owo.braid.framework.instance;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.Size;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2d;

public class WidgetTransform {
    protected double x = 0, y = 0;
    protected double width = 0, height = 0;

    public void setX(double x) {
        setState(() -> this.x = x);
    }

    public double x() {
        return this.x;
    }

    public void setY(double y) {
        setState(() -> this.y = y);
    }

    public double y() {
        return this.y;
    }

    public void setWidth(double width) {
        setState(() -> {
            if (Double.isInfinite(width)) {
                this.width = 69420;
                Owo.LOGGER.error("A widget transform received infinite width, clamping to 69420. This should never happen");
            } else {
                this.width = width;
            }
        });
    }

    public double width() {
        return this.width;
    }

    public void setHeight(double height) {
        setState(() -> {
            if (Double.isInfinite(height)) {
                this.height = 69420;
                Owo.LOGGER.error("A widget transform received infinite height, clamping to 69420. This should never happen");
            } else {
                this.height = height;
            }
        });
    }

    public double height() {
        return this.height;
    }

    public void setSize(Size size) {
        setState(() -> {
            this.width = size.width();
            this.height = size.height();
        });
    }

    public Size toSize() {
        return Size.of(this.width, this.height);
    }

    public void transformToParent(Matrix3x2f mat) {
        mat.translate((float) this.x, (float) this.y);
    }

    public void transformToParent(Matrix3x2fStack matrices) {
        matrices.translate((float) this.x, (float) this.y);
    }

    public void transformToWidget(Matrix3x2f mat) {
        mat.translate((float) -this.x, (float) -this.y);
    }

    public void transformToWidget(Matrix3x2fStack matrices) {
        matrices.translate((float) -this.x, (float) -this.y);
    }

    public void toParentCoordinates(Vector2d vec) {
        vec.add(this.x, this.y);
    }

    public void toWidgetCoordinates(Vector2d vec) {
        vec.sub(this.x, this.y);
    }

    public void setExtent(LayoutAxis axis, double value) {
        switch (axis) {
            case HORIZONTAL -> setWidth(value);
            case VERTICAL -> setHeight(value);
        }
    }

    public double getExtent(LayoutAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> width();
            case VERTICAL -> height();
        };
    }

    public void setCoordinate(LayoutAxis axis, double value) {
        switch (axis) {
            case HORIZONTAL -> setX(value);
            case VERTICAL -> setY(value);
        }
    }

    public double getCoordinate(LayoutAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> x();
            case VERTICAL -> y();
        };
    }

    protected void setState(Runnable action) {
        action.run();
        this.recompute();
    }

    public void recompute() {}
}
