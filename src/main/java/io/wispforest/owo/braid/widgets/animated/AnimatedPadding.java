package io.wispforest.owo.braid.widgets.animated;

import io.wispforest.owo.braid.animation.AutomaticallyAnimatedWidget;
import io.wispforest.owo.braid.animation.Easing;
import io.wispforest.owo.braid.animation.InsetsLerp;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Padding;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class AnimatedPadding extends AutomaticallyAnimatedWidget {
    public final Insets insets;
    public final @Nullable Widget child;

    public AnimatedPadding(Duration duration, Easing easing, Insets insets, @Nullable Widget child) {
        super(duration, easing);
        this.insets = insets;
        this.child = child;
    }

    @Override
    public State createState() {
        return new State();
    }

    public static class State extends AutomaticallyAnimatedWidget.State<AnimatedPadding> {

        private InsetsLerp insets;

        @Override
        protected void updateLerps() {
            this.insets = this.visitLerp(this.insets, this.widget().insets, InsetsLerp::new);
        }

        @Override
        public Widget build(BuildContext context) {
            return new Padding(
                this.insets.compute(this.animationValue()),
                this.widget().child
            );
        }
    }
}
