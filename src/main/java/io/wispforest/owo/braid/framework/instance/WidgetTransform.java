package io.wispforest.owo.braid.framework.instance;

import io.wispforest.owo.braid.core.Size;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public class WidgetTransform {
    protected @Nullable Matrix4f toParent;
    protected @Nullable Matrix4f toWidget;
    protected @Nullable Box aabb;

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
        setState(() -> this.width = width);
    }

    public double width() {
        return this.width;
    }

    public void setHeight(double height) {
        setState(() -> this.height = height);
    }

    public double height() {
        return this.height;
    }

    public Size toSize() {
        return Size.of(this.width, this.height);
    }

    public Matrix4fc toParent() {
        if (this.toParent == null) {
            this.toParent = new Matrix4f().translate((float) this.x, (float) this.y, 0);
        }

        return this.toParent;
    }

    public Matrix4fc toWidget() {
        if (this.toWidget == null) {
            this.toWidget = new Matrix4f(this.toParent()).invert();
        }

        return this.toWidget;
    }

    public Box aabb() {
        if (this.aabb == null) {
            var min = new Vector3f().mulPosition(this.toParent());
            var max = new Vector3f((float) this.x, (float) this.y, 0).mulPosition(this.toParent());

            this.aabb = new Box(min.x, min.y, min.z, max.x, max.y, max.z);
        }

        return this.aabb;
    }

    public void transformToParent(Matrix4f mat) {
        mat.translate((float) this.x, (float) this.y, 0);
    }

    public void transformToWidget(Matrix4f mat) {
        mat.translate((float) -this.x, (float) -this.y, 0);
    }

    public void toParentCoordinates(Vector3f vec) {
        vec.add((float) this.x, (float) this.y, 0);
    }

    public void toWidgetCoordinates(Vector3f vec) {
        vec.sub((float) this.x, (float) this.y, 0);
    }

    protected void setState(Runnable action) {
        action.run();
        this.recompute();
    }

    protected void recompute() {
        this.toParent = null;
        this.toWidget = null;
        this.aabb = null;
    }
}
