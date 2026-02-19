package io.wispforest.owo.braid.animation;

import io.wispforest.owo.braid.framework.proxy.ProxyHost;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class Animation {

    private final Scheduler scheduler;
    private final Listener listener;
    private final @Nullable FinishListener finishListener;

    public Easing easing;
    public Duration duration;

    private double progress;
    private @Nullable Target target;

    public Animation(Easing easing, Duration duration, Scheduler scheduler, Listener listener, @Nullable FinishListener finishListener, Target startFrom) {
        this.easing = easing;
        this.duration = duration;
        this.scheduler = scheduler;
        this.listener = listener;
        this.finishListener = finishListener;
        this.progress = startFrom.targetProgress;
    }

    public Animation(Easing easing, Duration duration, Scheduler scheduler, Listener listener, Target startFrom) {
        this(easing, duration, scheduler, listener, null, startFrom);
    }

    public @Nullable Target target() {
        return this.target;
    }

    public double progress() {
        return this.easing.apply((float) this.progress);
    }

    public void towards(Target target) {
        this.towards(target, true);
    }

    public void towards(Target target, boolean restart) {
        if (restart) {
            this.progress = 1 - target.targetProgress;
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

        this.progress = Mth.clamp(
            this.progress + this.target.direction * delta.toNanos() / (double) this.duration.toNanos(),
            0,
            1
        );

        this.listener.onUpdate(this.easing.apply((float) this.progress));

        if (Math.abs(this.progress - this.target.targetProgress) > EPSILON) {
            this.scheduler.schedule(this::callback);
        } else {
            if (this.finishListener != null) {
                this.finishListener.onFinished(this.target);
            }

            this.progress = this.target.targetProgress;
            this.target = null;
        }
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
    public interface FinishListener {
        void onFinished(Target atTarget);
    }

    @FunctionalInterface
    public interface Scheduler {
        void schedule(ProxyHost.AnimationCallback callback);
    }
}
