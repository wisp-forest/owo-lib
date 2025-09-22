package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.braid.animation.Animation;
import io.wispforest.owo.braid.animation.Easing;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.Clip;
import io.wispforest.owo.braid.widgets.basic.ListenableBuilder;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.scroll.RawScrollView;
import io.wispforest.owo.braid.widgets.scroll.ScrollController;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class Marquee extends StatefulWidget {

    protected Easing easing = Easing.IN_OUT_SINE;
    protected Duration minDuration = Duration.ofSeconds(1);
    protected Duration durationPerPixel = Duration.ofMillis(100);
    protected Duration pauseTime = Duration.ofSeconds(2);
    protected boolean pauseWhileHovered = true;
    protected LayoutAxis axis = LayoutAxis.HORIZONTAL;
    public final Widget child;

    public Marquee(@Nullable WidgetSetupCallback<Marquee> setup, Widget child) {
        this.child = child;
        if (setup != null) setup.setup(this);
    }

    public Marquee(Widget child) {
        this.child = child;
    }

    public Marquee easing(Easing easing) {
        this.assertMutable();
        this.easing = easing;
        return this;
    }

    public Easing easing() {
        return this.easing;
    }

    public Marquee minDuration(Duration minDuration) {
        this.assertMutable();
        this.minDuration = minDuration;
        return this;
    }

    public Marquee minDuration(long millis) {
        return this.minDuration(Duration.ofMillis(millis));
    }

    public Duration minDuration() {
        return this.minDuration;
    }

    public Marquee durationPerPixel(Duration durationPerPixel) {
        this.assertMutable();
        this.durationPerPixel = durationPerPixel;
        return this;
    }

    public Marquee durationPerPixel(long millisPerPixel) {
        return this.durationPerPixel(Duration.ofMillis(millisPerPixel));
    }

    public Duration durationPerPixel() {
        return this.durationPerPixel;
    }

    public Marquee pauseTime(Duration pauseTime) {
        this.assertMutable();
        this.pauseTime = pauseTime;
        return this;
    }

    public Marquee pauseTime(long millis) {
        return this.pauseTime(Duration.ofMillis(millis));
    }

    public Duration pauseTime() {
        return this.pauseTime;
    }

    public Marquee pauseWhileHovered(boolean pauseWhileHovered) {
        this.pauseWhileHovered = pauseWhileHovered;
        return this;
    }

    public boolean pauseWhileHovered() {
        return this.pauseWhileHovered;
    }

    public Marquee axis(LayoutAxis axis) {
        this.assertMutable();
        this.axis = axis;
        return this;
    }

    public LayoutAxis axis() {
        return this.axis;
    }

    @Override
    public WidgetState<Marquee> createState() {
        return new State();
    }

    public static class State extends WidgetState<Marquee> {

        private final ScrollController controller = new ScrollController(this);
        private Animation animation;
        private Animation.Target pausedAnimationTarget = null;

        private long callbackId = -1;

        @Override
        public void init() {
            this.animation = new Animation(
                this.widget().easing,
                this.widget().durationPerPixel,
                this::scheduleAnimationCallback,
                this::onAnimationStep,
                this::onAnimationFinished,
                Animation.Target.START
            );

            this.controller.addListener(() -> {
                this.updateAnimationDuration();
                if (this.animation.target() == null) {
                    this.cancelDelayedCallback(this.callbackId);
                    this.animation.towards(this.animation.progress() == 0 ? Animation.Target.END : Animation.Target.START);
                }
            });
        }

        @Override
        public void didUpdateWidget(Marquee oldWidget) {
            super.didUpdateWidget(oldWidget);

            this.updateAnimationDuration();
            this.animation.easing = this.widget().easing;
        }

        private void updateAnimationDuration() {
            this.animation.duration = Duration.ofNanos((long) Math.max(
                this.widget().minDuration.toNanos(),
                this.widget().durationPerPixel.toNanos() * this.controller.maxOffset()
            ));
        }

        private void onAnimationStep(double progress) {
            this.controller.jumpTo(progress * this.controller.maxOffset());
        }

        private void onAnimationFinished(Animation.Target atTarget) {
            if (this.controller.maxOffset() == 0) return;

            this.cancelDelayedCallback(this.callbackId);
            this.callbackId = this.scheduleDelayedCallback(
                this.widget().pauseTime,
                () -> this.animation.towards(atTarget == Animation.Target.END ? Animation.Target.START : Animation.Target.END)
            );
        }

        @Override
        public Widget build(BuildContext context) {
            return new MouseArea(
                widget -> {
                    if (!this.widget().pauseWhileHovered) return;
                    widget
                        .enterCallback(() -> {
                            this.pausedAnimationTarget = this.animation.target();
                            this.animation.pause();
                        })
                        .exitCallback(() -> {
                            if (this.pausedAnimationTarget == null) return;
                            this.animation.towards(this.pausedAnimationTarget, false);
                        });
                },
                new Clip(
                    new ListenableBuilder(
                        this.controller,
                        buildContext -> new RawScrollView(
                            this.widget().axis == LayoutAxis.HORIZONTAL ? this.controller : null,
                            this.widget().axis == LayoutAxis.VERTICAL ? this.controller : null,
                            this.widget().child
                        )
                    )
                )
            );
        }
    }
}
