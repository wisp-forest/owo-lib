package io.wispforest.owo.braid.widgets.physics;

import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class PhysicsWorld {
    public List<RigidBody> bodies = new ArrayList<>();
    public Vector2d gravity = new Vector2d(0, 500.0); // Pixels per second^2

    public void step(double dt) {
        while (dt > 0) {
            var actualDt = Math.min(dt, 0.01);
            dt -= 0.01;

            // Integrate
            for (RigidBody b : bodies) {
                if (b.invMass != 0) {
                    b.vel.add(new Vector2d(gravity).mul(actualDt));
                    b.pos.add(new Vector2d(b.vel).mul(actualDt));
                    b.angle += b.angularVel * actualDt;
                }
            }

            // Solve collisions 8 times per frame for stability
            int iterations = 4;
            for (int i = 0; i < iterations; i++) {
                for (int a = 0; a < bodies.size(); a++) {
                    for (int b = a + 1; b < bodies.size(); b++) {
                        RigidBody rA = bodies.get(a);
                        RigidBody rB = bodies.get(b);
                        if (rA.invMass == 0 && rB.invMass == 0) continue;

                        CollisionManifold manifold = CollisionDetector.checkSAT(rA, rB);
                        if (manifold.collided) {
                            CollisionResolver.resolve(manifold);
                        }
                    }
                }
            }
        }
    }
}
