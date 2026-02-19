package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.animation.Animation;
import io.wispforest.owo.braid.animation.DoubleLerp;
import io.wispforest.owo.braid.animation.Easing;
import io.wispforest.owo.braid.core.Listenable;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import net.minecraft.util.Mth;

import java.time.Duration;

public class ScrollController extends Listenable {

    private final Animation animation;
    private DoubleLerp lerp;

    public ScrollController(WidgetState<?> contextState) {
        this(contextState::scheduleAnimationCallback);
    }

    public ScrollController(Animation.Scheduler callbackScheduler) {
        this.lerp = new DoubleLerp(0.0, 0.0);
        this.animation = new Animation(
            Easing.LINEAR,
            Duration.ofNanos(1),
            callbackScheduler,
            progress -> this.setOffset(this.lerp.compute(progress)),
            Animation.Target.END
        );
    }

    protected double offset = 0;
    protected double maxOffset = 0;

    public void animateTo(double offset, Duration duration, Easing easing) {
        this.animation.duration = duration;
        this.animation.easing = easing;
        this.lerp = new DoubleLerp(this.offset, this.clampOffset(offset));

        this.animation.towards(Animation.Target.END);
    }

    public void animateBy(double by, Duration duration, Easing easing) {
        this.animateTo(this.lerp.end + by, duration, easing);
    }

    public void jumpTo(double offset) {
        offset = this.clampOffset(offset);

        this.animation.stop();
        this.lerp = new DoubleLerp(offset, offset);

        this.setOffset(offset);
    }

    public void jumpBy(double by) {
        this.jumpTo(this.offset + by);
    }

    private void setOffset(double offset) {
        if (this.offset == offset) {
            return;
        }

        this.offset = this.clampOffset(offset);
        this.notifyListeners();
    }

    private double clampOffset(double offset) {
        return Mth.clamp(offset, 0, this.maxOffset);
    }

    public double offset() {
        return this.offset;
    }

    boolean setMaxOffset(double maxOffset) {
        if (this.maxOffset == maxOffset) {
            return false;
        }

        this.maxOffset = maxOffset;
        this.offset = this.clampOffset(this.offset);

        return true;
    }

    boolean maxOffsetNotificationScheduled = false;
    void sendMaxOffsetNotification() {
        this.notifyListeners();
        this.maxOffsetNotificationScheduled = false;
    }

    public double maxOffset() {
        return this.maxOffset;
    }
}
