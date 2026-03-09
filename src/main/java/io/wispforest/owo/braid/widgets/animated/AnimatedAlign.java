package io.wispforest.owo.braid.widgets.animated;

import io.wispforest.owo.braid.animation.*;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.OptionalDouble;

public class AnimatedAlign extends AutomaticallyAnimatedWidget {

    public final Alignment alignment;
    public final @Nullable Double widthFactor;
    public final @Nullable Double heightFactor;
    public final Widget child;

    public AnimatedAlign(Duration duration, Easing easing, Alignment alignment, @Nullable Double widthFactor, @Nullable Double heightFactor, Widget child) {
        super(duration, easing);
        this.alignment = alignment;
        this.widthFactor = widthFactor;
        this.heightFactor = heightFactor;
        this.child = child;
    }

    public AnimatedAlign(Duration duration, Easing easing, Alignment alignment, Widget child) {
        this(duration, easing, alignment, null, null, child);
    }

    @Override
    public State createState() {
        return new State();
    }

    public static class State extends AutomaticallyAnimatedWidget.State<AnimatedAlign> {

        private AlignmentLerp alignment;
        private Lerp<@Nullable Double> widthFactor;
        private Lerp<@Nullable Double> heightFactor;

        @Override
        protected void updateLerps() {
            this.alignment = this.visitLerp(this.alignment, this.widget().alignment, AlignmentLerp::new);
            this.widthFactor = this.visitNullableLerp(this.widthFactor, this.widget().widthFactor, DoubleLerp::new);
            this.heightFactor = this.visitNullableLerp(this.heightFactor, this.widget().heightFactor, DoubleLerp::new);
        }

        @Override
        public Widget build(BuildContext context) {
            return new Align(
                this.alignment.compute(this.animationValue()),
                this.widthFactor.compute(this.animationValue()),
                this.heightFactor.compute(this.animationValue()),
                this.widget().child
            );
        }
    }
}
