package io.wispforest.owo.braid.widgets.physics;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import org.joml.Vector2d;

public class RigidBody {
    public Vector2d pos = new Vector2d();
    public Vector2d vel = new Vector2d();
    public double angle = 0; // Radians
    public double angularVel = 0;

    public double width, height;
    public double invMass, invInertia;
    public double restitution = 0.3; // Bounciness (0 to 1)
    public double staticFriction = 0.5;
    public double dynamicFriction = 0.3;

    public Widget widget = new Box(Color.WHITE);

    public RigidBody(double x, double y, double w, double h, double mass) {
        this.pos.set(x, y);
        this.width = w;
        this.height = h;
        if (mass > 0) {
            this.invMass = 1.0 / mass;
            // Moment of Inertia for a rectangle: (1/12) * m * (w^2 + h^2)
            this.invInertia = 1.0 / ((1.0 / 12.0) * mass * (w * w + h * h));
        } else {
            this.invMass = 0; // Infinite mass
            this.invInertia = 0;
        }
    }

    public Vector2d[] getVertices() {
        Vector2d[] v = new Vector2d[4];
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double hw = width / 2.0;
        double hh = height / 2.0;

        // Bottom Left, Bottom Right, Top Right, Top Left (Local)
        Vector2d[] local = {
            new Vector2d(-hw, -hh), new Vector2d(hw, -hh),
            new Vector2d(hw, hh), new Vector2d(-hw, hh)
        };

        for (int i = 0; i < 4; i++) {
            double rx = local[i].x * cos - local[i].y * sin;
            double ry = local[i].x * sin + local[i].y * cos;
            v[i] = new Vector2d(rx + pos.x, ry + pos.y);
        }
        return v;
    }
}