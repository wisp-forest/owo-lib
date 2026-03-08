package io.wispforest.owo.braid.widgets.physics;

import org.joml.Vector2d;

public class CollisionDetector {
    public static CollisionManifold checkSAT(RigidBody a, RigidBody b) {
        CollisionManifold manifold = new CollisionManifold();
        manifold.a = a;
        manifold.b = b;

        Vector2d[] vA = a.getVertices();
        Vector2d[] vB = b.getVertices();

        double minOverlap = Double.MAX_VALUE;
        Vector2d bestAxis = new Vector2d();

        // 1. Check all 4 axes (2 from A, 2 from B)
        Vector2d[] axes = {
            new Vector2d(vA[1]).sub(vA[0]).normalize(), // A right
            new Vector2d(vA[3]).sub(vA[0]).normalize(), // A up
            new Vector2d(vB[1]).sub(vB[0]).normalize(), // B right
            new Vector2d(vB[3]).sub(vB[0]).normalize()  // B up
        };

        for (Vector2d axis : axes) {
            double[] projA = project(vA, axis);
            double[] projB = project(vB, axis);

            if (projA[1] < projB[0] || projB[1] < projA[0]) return manifold; // Gap!

            double overlap = Math.min(projA[1], projB[1]) - Math.max(projA[0], projB[0]);
            if (overlap < minOverlap) {
                minOverlap = overlap;
                bestAxis.set(axis);
            }
        }

        manifold.collided = true;
        manifold.depth = minOverlap;
        manifold.normal.set(bestAxis);

        // Ensure normal points from A to B
        Vector2d dir = new Vector2d(b.pos).sub(a.pos);
        if (dir.dot(manifold.normal) < 0) manifold.normal.mul(-1);

        manifold.contactPoint = findContactPoint(vA, vB);
        return manifold;
    }

    private static double[] project(Vector2d[] vertices, Vector2d axis) {
        double min = vertices[0].dot(axis);
        double max = min;
        for (int i = 1; i < 4; i++) {
            double p = vertices[i].dot(axis);
            min = Math.min(min, p);
            max = Math.max(max, p);
        }
        return new double[]{min, max};
    }

    private static Vector2d findContactPoint(Vector2d[] vA, Vector2d[] vB) {
        Vector2d averageContact = new Vector2d(0, 0);
        int count = 0;

        // Check which vertices of A are inside B
        for (Vector2d v : vA) {
            if (isPointInBox(v, vB)) {
                averageContact.add(v);
                count++;
            }
        }
        // Check which vertices of B are inside A
        for (Vector2d v : vB) {
            if (isPointInBox(v, vA)) {
                averageContact.add(v);
                count++;
            }
        }

        if (count > 0) {
            return averageContact.div(count);
        }
        return new Vector2d(vA[0]).add(vB[0]).mul(0.5); // Fallback
    }

    private static boolean isPointInBox(Vector2d p, Vector2d[] box) {
        // Basic point-in-polygon check for convex box
        for (int i = 0; i < 4; i++) {
            Vector2d v1 = box[i];
            Vector2d v2 = box[(i + 1) % 4];
            Vector2d edge = new Vector2d(v2).sub(v1);
            Vector2d toPoint = new Vector2d(p).sub(v1);
            if (edge.x * toPoint.y - edge.y * toPoint.x < 0) return false;
        }
        return true;
    }
}
