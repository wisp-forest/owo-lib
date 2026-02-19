package io.wispforest.owo.braid.display;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public final class DisplayQuad {
    public final Vec3 pos;
    public final Vec3 top;
    public final Vec3 left;
    public final Vec3 normal;

    public DisplayQuad(Vec3 pos, Vec3 top, Vec3 left) {
        this.pos = pos;
        this.top = top;
        this.left = left;
        this.normal = this.left.cross(this.top);
    }

    public Vec3 unproject(Vector2dc point) {
        return this.pos.add(this.top.scale(point.x())).add(this.left.scale(point.y()));
    }

    public @Nullable HitTestResult hitTest(Vec3 origin, Vec3 direction) {
        var t = this.pos.subtract(origin).dot(this.normal) / direction.dot(this.normal);
        if (t < 0) return null;

        var candidatePoint = origin.add(direction.scale(t)).subtract(this.pos);

        var widthSquared = this.top.lengthSqr();
        var heightSquared = this.left.lengthSqr();

        var point = new Vector2d(
            candidatePoint.dot(this.top) / widthSquared,
            candidatePoint.dot(this.left) / heightSquared
        );

        return point.x > 0 && point.x < 1 && point.y > 0 && point.y < 1
            ? new HitTestResult(point, t)
            : null;
    }

    public record HitTestResult(Vector2dc point, double t) {}
}
