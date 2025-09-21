package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.braid.animation.Animation;
import io.wispforest.owo.braid.animation.Easing;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Clip;
import io.wispforest.owo.braid.widgets.basic.ListenableBuilder;
import io.wispforest.owo.braid.widgets.scroll.RawScrollView;
import io.wispforest.owo.braid.widgets.scroll.ScrollController;

import java.time.Duration;

public class Marquee extends StatefulWidget {

    public final Easing easing;
    public final Duration minDuration;
    public final Duration durationPerPixel;
    public final Duration pauseTime;
    public final LayoutAxis axis;
    public final Widget child;

    public Marquee(Easing easing, Duration minDuration, Duration durationPerPixel, Duration pauseTime, LayoutAxis axis, Widget child) {
        this.easing = easing;
        this.minDuration = minDuration;
        this.durationPerPixel = durationPerPixel;
        this.pauseTime = pauseTime;
        this.axis = axis;
        this.child = child;
    }

    public Marquee(LayoutAxis axis, Widget child) {
        this(
            Easing.IN_OUT_SINE,
            Duration.ofSeconds(1),
            Duration.ofMillis(100),
            Duration.ofSeconds(2),
            axis,
            child
        );
    }

    public Marquee(Widget child) {
        this(LayoutAxis.HORIZONTAL, child);
    }

    @Override
    public WidgetState<Marquee> createState() {
        return new State();
    }

    public static class State extends WidgetState<Marquee> {

        private final ScrollController controller = new ScrollController(this);
        private Animation animation;

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
            return new Clip(
                new ListenableBuilder(
                    this.controller,
                    buildContext -> new RawScrollView(
                        this.widget().axis == LayoutAxis.HORIZONTAL ? this.controller : null,
                        this.widget().axis == LayoutAxis.VERTICAL ? this.controller : null,
                        this.widget().child
                    )
                )
            );
        }
    }
}