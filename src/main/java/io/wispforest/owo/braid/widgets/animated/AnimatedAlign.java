package io.wispforest.owo.braid.widgets.animated;

import io.wispforest.owo.braid.animation.AlignmentLerp;
import io.wispforest.owo.braid.animation.AutomaticallyAnimatedWidget;
import io.wispforest.owo.braid.animation.Easing;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;

import java.time.Duration;

public class AnimatedAlign extends AutomaticallyAnimatedWidget {

    public final Alignment alignment;
    public final Widget child;

    public AnimatedAlign(Duration duration, Easing easing, Alignment alignment, Widget child) {
        super(duration, easing);
        this.alignment = alignment;
        this.child = child;
    }

    @Override
    public State createState() {
        return new State();
    }

    public static class State extends AutomaticallyAnimatedWidget.State<AnimatedAlign> {

        private AlignmentLerp alignment;

        @Override
        protected void updateLerps() {
            this.alignment = this.visitLerp(this.alignment, this.widget().alignment, AlignmentLerp::new);
        }

        @Override
        public Widget build(BuildContext context) {
            return new Align(
                this.alignment.compute(this.animationValue()),
                this.widget().child
            );
        }
    }
}
