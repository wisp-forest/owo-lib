package io.wispforest.owo.braid.display;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public final class DisplayQuad {
    public final Vec3d pos;
    public final Vec3d top;
    public final Vec3d left;
    public final Vec3d normal;

    public DisplayQuad(Vec3d pos, Vec3d top, Vec3d left) {
        this.pos = pos;
        this.top = top;
        this.left = left;
        this.normal = this.left.crossProduct(this.top);
    }

    public Vec3d unproject(Vector2dc point) {
        return this.pos.add(this.top.multiply(point.x())).add(this.left.multiply(point.y()));
    }

    public @Nullable HitTestResult hitTest(Vec3d origin, Vec3d direction) {
        var t = this.pos.subtract(origin).dotProduct(this.normal) / direction.dotProduct(this.normal);
        if (t < 0) return null;

        var candidatePoint = origin.add(direction.multiply(t)).subtract(this.pos);

        var widthSquared = this.top.lengthSquared();
        var heightSquared = this.left.lengthSquared();

        var point = new Vector2d(
            candidatePoint.dotProduct(this.top) / widthSquared,
            candidatePoint.dotProduct(this.left) / heightSquared
        );

        return point.x > 0 && point.x < 1 && point.y > 0 && point.y < 1
            ? new HitTestResult(point, t)
            : null;
    }

    public record HitTestResult(Vector2dc point, double t) {}
}
