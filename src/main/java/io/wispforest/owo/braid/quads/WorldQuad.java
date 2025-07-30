package io.wispforest.owo.braid.quads;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public final class WorldQuad {
    public final Vec3d pos;
    public final Vec3d top;
    public final Vec3d left;
    public final Vec3d normal;

    public WorldQuad(Vec3d pos, Vec3d top, Vec3d left) {
        this.pos = pos;
        this.top = top;
        this.left = left;
        this.normal = this.top.crossProduct(this.left);
    }

    public Vec3d unproject(Vector2dc point) {
        return this.pos.add(this.top.multiply(point.x())).add(this.left.multiply(point.y()));
    }

    public @Nullable Vector2dc intersect(Ray ray) {
        var t = this.pos.subtract(ray.origin()).dotProduct(this.normal) / ray.direction().dotProduct(this.normal);
        if (t < 0) return null;

        var candidatePoint = ray.origin().add(ray.direction().multiply(t)).subtract(this.pos);

        var widthSquared = this.top.lengthSquared();
        var heightSquared = this.left.lengthSquared();

        var pointX = candidatePoint.dotProduct(this.top) / widthSquared;
        var pointY = candidatePoint.dotProduct(this.left) / heightSquared;

        return pointX > 0 && pointX < 1 && pointY > 0 && pointY < 1
            ? new Vector2d(pointX, pointY)
            : null;
    }
}
