package io.wispforest.owo.braid.core;

import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public class Aabb2d {

    public double x;
    public double y;
    public double width;
    public double height;

    public Aabb2d(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double minX() {
        return this.x;
    }

    public double maxX() {
        return this.x + this.width;
    }

    public double minY() {
        return this.y;
    }

    public double maxY() {
        return this.y + this.height;
    }

    public Aabb2d transform(Matrix3x2f matrix) {
        var topLeft = matrix.transformPosition((float) this.x, (float) this.y, new Vector2f());
        var topRight = matrix.transformPosition((float) (this.x + this.width), (float) this.y, new Vector2f());
        var bottomLeft = matrix.transformPosition((float) this.x, (float) (this.y + this.height), new Vector2f());
        var bottomRight = matrix.transformPosition((float) (this.x + this.width), (float) (this.y + this.height), new Vector2f());

        this.x = Math.min(Math.min(Math.min(topLeft.x, topRight.x), bottomLeft.x), bottomRight.x);
        this.width = Math.max(Math.max(Math.max(topLeft.x, topRight.x), bottomLeft.x), bottomRight.x) - this.x;

        this.y = Math.min(Math.min(Math.min(topLeft.y, topRight.y), bottomLeft.y), bottomRight.y);
        this.height = Math.max(Math.max(Math.max(topLeft.y, topRight.y), bottomLeft.y), bottomRight.y) - this.y;

        return this;
    }
}
