package io.wispforest.owo.braid.widgets.physics;

import org.joml.Vector2d;

public class CollisionManifold {
    public RigidBody a, b;
    public Vector2d normal = new Vector2d();
    public double depth;
    public Vector2d contactPoint = new Vector2d();
    public boolean collided = false;
}
