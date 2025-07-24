package io.wispforest.owo.braid.animation;

import io.wispforest.owo.braid.framework.proxy.ProxyHost;
import io.wispforest.owo.ui.core.Easing;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class Animation {

    private final Scheduler scheduler;
    private final Listener listener;

    public Easing easing;
    public Duration duration;

    private Duration elapsedTime = Duration.ZERO;
    private double progress;
    private @Nullable Target target;

    public Animation(Easing easing, Duration duration, Scheduler scheduler, Listener listener, Target startFrom) {
        this.easing = easing;
        this.duration = duration;
        this.scheduler = scheduler;
        this.listener = listener;
        this.progress = startFrom.targetProgress;
    }

    public double progress() {
        return this.progress;
    }

    public void towards(Target target) {
        this.towards(target, true);
    }

    public void towards(Target target, boolean restart) {
        if (restart) {
            this.progress = 1 - target.targetProgress;
            this.elapsedTime = Duration.ZERO;
        }

        if (this.target == null) {
            this.scheduler.schedule(this::callback);
        }

        this.target = target;
    }

    public void pause() {
        this.target = null;
    }

    public void stop() {
        this.stop(null);
    }

    public void stop(@Nullable Target at) {
        if (this.target == null && at == null) return;

        this.progress = at != null ? at.targetProgress : this.target.targetProgress;
        this.target = null;
    }

    private void callback(Duration delta) {
        if (this.target == null) return;

        this.elapsedTime = this.elapsedTime.plus(delta.multipliedBy(this.target.direction));
        this.progress = this.easing.apply((float) (this.elapsedTime.toNanos() / (double) this.duration.toNanos()));

        if (Math.abs(this.progress - this.target.targetProgress) > EPSILON) {
            this.scheduler.schedule(this::callback);
        } else {
            this.progress = this.target.targetProgress;
            this.target = null;
        }

        this.listener.onUpdate(this.progress);
    }

    // ---

    private static final double EPSILON = 1e-3;

    // ---

    public enum Target {
        START(-1, 0),
        END(1, 1);

        public final long direction;
        public final double targetProgress;

        Target(long direction, double targetProgress) {
            this.direction = direction;
            this.targetProgress = targetProgress;
        }
    }

    @FunctionalInterface
    public interface Listener {
        void onUpdate(double progress);
    }

    @FunctionalInterface
    public interface Scheduler {
        void schedule(ProxyHost.AnimationCallback callback);
    }
}
