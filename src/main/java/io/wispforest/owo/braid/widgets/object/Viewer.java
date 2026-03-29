package io.wispforest.owo.braid.widgets.object;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.time.Duration;
import java.util.function.Consumer;

import static com.mojang.math.Constants.EPSILON;

public class Viewer extends StatefulWidget {
    public final ViewerBuilder builder;

    public Viewer(ViewerBuilder builder) {
        this.builder = builder;
    }

    @Override
    public WidgetState<Viewer> createState() {
        return new State();
    }

    public static class State extends WidgetState<Viewer> {

        private final Quaternionf rotation = new Quaternionf();
        private final Quaternionf dragStartRotation = new Quaternionf();
        private final Vector3f dragStartSpherePos = new Vector3f();

        private final Vector3f prevSpherePos = new Vector3f();
        private final Vector3f currSpherePos = new Vector3f();
        private long prevTimeNs = 0;
        private long currTimeNs = 0;

        private final Vector3f angularVelocity = new Vector3f();

        private double zoom = 1;
        private double zoomVelocity = 0;

        private boolean releasedLate = false;

        private float[] normalizeScreenPos(double cursorX, double cursorY, double width, double height) {
            double radius = Math.min(width, height) * 0.5;
            return new float[]{
                (float) ((cursorX - width  * 0.5) / radius),
                (float) ((height * 0.5 - cursorY) / radius)
            };
        }

        private void animate(Duration delta) {
            setState(() -> {
                var seconds = delta.toNanos() / 1e9;
                var needsMore = false;

                float speed = this.angularVelocity.length();
                if (speed > EPSILON) {
                    var newRot = new Quaternionf().rotateAxis(
                        speed * (float) seconds,
                        new Vector3f(this.angularVelocity).normalize()
                    );
                    newRot.mul(this.rotation, this.rotation);
                    this.angularVelocity.mul((float) Math.exp(-seconds * 5));
                    if (this.angularVelocity.length() > EPSILON) needsMore = true;
                }

                this.zoom = Math.max(0.1, this.zoom + this.zoomVelocity * seconds);
                this.zoomVelocity *= Math.exp(-seconds * 8);
                if (Math.abs(this.zoomVelocity) > 1e-4) needsMore = true;

                if (needsMore) {
                    this.scheduleAnimationCallback(this::animate);
                }
            });
        }

        @Override
        public Widget build(BuildContext context) {
            return new MouseArea(
                widget -> widget
                    .clickCallback((x, y, button, modifiers) -> {
                        this.angularVelocity.set(0, 0, 0);
                        this.zoomVelocity = 0;

                        var bounds = this.context().instance().computeGlobalBounds();
                        var sc = normalizeScreenPos(x, y, bounds.getXsize(), bounds.getYsize());

                        this.dragStartRotation.set(this.rotation);
                        this.dragStartSpherePos.set(sc[0], sc[1], 0);

                        this.prevSpherePos.set(sc[0], sc[1], 0);
                        this.currSpherePos.set(sc[0], sc[1], 0);
                        this.prevTimeNs = this.currTimeNs = System.nanoTime();

                        return true;
                    })
                    .dragCallback((x, y, dx, dy) -> {
                        var bounds = this.context().instance().computeGlobalBounds();
                        var sc = normalizeScreenPos(x, y, bounds.getXsize(), bounds.getYsize());

                        var totalDx = sc[0] - this.dragStartSpherePos.x;
                        var totalDy = sc[1] - this.dragStartSpherePos.y;
                        var dist = (float) Math.sqrt(totalDx * totalDx + totalDy * totalDy);

                        if (dist > EPSILON) {
                            var newRot = new Quaternionf().rotateAxis(dist, new Vector3f(-totalDy / dist, totalDx / dist, 0));
                            setState(() -> newRot.mul(this.dragStartRotation, this.rotation));
                        }

                        this.prevSpherePos.set(this.currSpherePos);
                        this.prevTimeNs = this.currTimeNs;
                        this.currSpherePos.set(sc[0], sc[1], 0);
                        this.currTimeNs = System.nanoTime();

                        this.releasedLate = false;
                        this.scheduleDelayedCallback(Duration.ofMillis(120), () -> this.releasedLate = true);
                    })
                    .dragEndCallback(() -> {
                        if (this.releasedLate) return;

                        var dt = this.currTimeNs - this.prevTimeNs;
                        if (dt <= 0 || dt >= 120_000_000L) return;

                        var ddx = this.currSpherePos.x - this.prevSpherePos.x;
                        var ddy = this.currSpherePos.y - this.prevSpherePos.y;
                        var dist = (float) Math.sqrt(ddx * ddx + ddy * ddy);
                        if (dist < EPSILON) return;

                        float speed = Math.min(dist / (dt / 1e9f), 20f);
                        this.angularVelocity.set(new Vector3f(-ddy / dist, ddx / dist, 0).mul(speed));
                        this.scheduleAnimationCallback(this::animate);
                    })
                    .scrollCallback((horizontal, vertical) -> {
                        setState(() -> this.zoomVelocity += vertical * 0.8);
                        this.scheduleAnimationCallback(this::animate);
                        return true;
                    }),
                this.widget().builder.build(
                    transform -> {
                        transform.scale((float) this.zoom);
                        transform.rotate(this.rotation);
                    }
                )
            );
        }
    }

    @FunctionalInterface
    public interface ViewerBuilder {
        Widget build(Consumer<Matrix4f> transform);
    }
}
