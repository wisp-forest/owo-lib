package io.wispforest.owo.braid.widgets.physics;

import org.joml.Vector2d;

public class CollisionResolver {
    public static void resolve(CollisionManifold m) {
        RigidBody a = m.a;
        RigidBody b = m.b;

        // 1. Positional Correction (Sinking Fix)
        double slack = 0.01; // Allow 0.01px of overlap
        double percent = 0.4; // Correct 40% per step
        Vector2d correction = new Vector2d(m.normal).mul(Math.max(m.depth - slack, 0) / (a.invMass + b.invMass) * percent);
        a.pos.sub(new Vector2d(correction).mul(a.invMass));
        b.pos.add(new Vector2d(correction).mul(b.invMass));

        // 2. Setup variables
        Vector2d ra = new Vector2d(m.contactPoint).sub(a.pos);
        Vector2d rb = new Vector2d(m.contactPoint).sub(b.pos);

        Vector2d va = new Vector2d(-ra.y, ra.x).mul(a.angularVel).add(a.vel);
        Vector2d vb = new Vector2d(-rb.y, rb.x).mul(b.angularVel).add(b.vel);
        Vector2d relVel = new Vector2d(vb).sub(va);

        double contactVel = relVel.dot(m.normal);
        if (contactVel > 0) return; // Already moving apart

        // 3. Normal Impulse (J)
        double e = Math.min(a.restitution, b.restitution);
        double raCrossN = ra.x * m.normal.y - ra.y * m.normal.x;
        double rbCrossN = rb.x * m.normal.y - rb.y * m.normal.x;
        double invInertiaSum = (raCrossN * raCrossN * a.invInertia) + (rbCrossN * rbCrossN * b.invInertia);

        double j = -(1.0 + e) * contactVel / (a.invMass + b.invMass + invInertiaSum);

        Vector2d impulse = new Vector2d(m.normal).mul(j);
        applyImpulse(a, b, ra, rb, impulse);

        // 4. Friction Impulse (re-calculate relative velocity)
        va = new Vector2d(-ra.y, ra.x).mul(a.angularVel).add(a.vel);
        vb = new Vector2d(-rb.y, rb.x).mul(b.angularVel).add(b.vel);
        relVel = new Vector2d(vb).sub(va);

        Vector2d tangent = new Vector2d(relVel).sub(new Vector2d(m.normal).mul(relVel.dot(m.normal)));
        if (tangent.lengthSquared() > 0.0001) {
            tangent.normalize();
            double vt = relVel.dot(tangent);
            double raCrossT = ra.x * tangent.y - ra.y * tangent.x;
            double rbCrossT = rb.x * tangent.y - rb.y * tangent.x;
            double invInertiaSumT = (raCrossT * raCrossT * a.invInertia) + (rbCrossT * rbCrossT * b.invInertia);

            double jt = -vt / (a.invMass + b.invMass + invInertiaSumT);

            // Coulomb's Law
            double mu = (a.staticFriction + b.staticFriction) / 2.0;
            if (Math.abs(jt) > j * mu) jt = -j * ((a.dynamicFriction + b.dynamicFriction) / 2.0);

            applyImpulse(a, b, ra, rb, new Vector2d(tangent).mul(jt));
        }
    }

    private static void applyImpulse(RigidBody a, RigidBody b, Vector2d ra, Vector2d rb, Vector2d impulse) {
        a.vel.sub(new Vector2d(impulse).mul(a.invMass));
        a.angularVel -= (ra.x * impulse.y - ra.y * impulse.x) * a.invInertia;
        b.vel.add(new Vector2d(impulse).mul(b.invMass));
        b.angularVel += (rb.x * impulse.y - rb.y * impulse.x) * b.invInertia;
    }
}
