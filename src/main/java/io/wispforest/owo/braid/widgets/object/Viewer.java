package io.wispforest.owo.braid.widgets.object;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.ui.util.Delta;
import org.joml.Matrix4f;

import java.time.Duration;
import java.util.function.Consumer;

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

        private double pitch = 35;
        private double pitchDelta = 0;

        private double yaw = -45;
        private double yawDelta = 0;

        private double zoom = 1;
        private double zoomDelta = 0;

        private boolean releasedLate = false;

        private void animate(Duration delta) {
            setState(() -> {
                var seconds = delta.toNanos() / (double) Duration.ofSeconds(1).toNanos();
                this.pitch += this.pitchDelta * seconds * 10;
                this.yaw += this.yawDelta * seconds * 10;
                this.zoom += this.zoomDelta * seconds * 10;

                this.pitchDelta += Delta.compute(this.pitchDelta, 0, seconds * .5);
                this.yawDelta += Delta.compute(this.yawDelta, 0, seconds * .5);
                this.zoomDelta += Delta.compute(this.zoomDelta, 0, seconds * .5);

                if (Math.abs(this.pitchDelta) > 1e-4 || Math.abs(this.yawDelta) > 1e-4 || Math.abs(this.zoomDelta) > 1e-4) {
                    this.scheduleAnimationCallback(this::animate);
                }
            });
        }

        @Override
        public Widget build(BuildContext context) {
            return new MouseArea(
                widget -> widget
                    .clickCallback((x, y, button, modifiers) -> {
                        this.yawDelta = 0;
                        this.pitchDelta = 0;

                        return true;
                    })
                    .dragCallback((x, y, dx, dy) -> {
                        this.yaw += dx * .5;
                        this.pitch += dy * .5;

                        this.yawDelta = dx;
                        this.pitchDelta = dy;

                        this.releasedLate = false;
                        this.scheduleDelayedCallback(
                            Duration.ofMillis(200), () -> {
                                this.releasedLate = true;
                            }
                        );
                    })
                    .dragEndCallback(() -> {
                        if (this.releasedLate) return;
                        this.animate(Duration.ZERO);
                    })
                    .scrollCallback((horizontal, vertical) -> {
                        setState(() -> this.zoomDelta += vertical * .1);

                        return true;
                    }),
                this.widget().builder.build(
                    transform -> {
                        transform.scale((float) (this.zoom));
                        transform.rotateX((float) Math.toRadians(this.pitch));
                        transform.rotateY((float) Math.toRadians(this.yaw));
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
