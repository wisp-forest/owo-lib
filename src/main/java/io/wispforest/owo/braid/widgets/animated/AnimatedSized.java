package io.wispforest.owo.braid.widgets.animated;

import io.wispforest.owo.braid.animation.AutomaticallyAnimatedWidget;
import io.wispforest.owo.braid.animation.DoubleLerp;
import io.wispforest.owo.braid.animation.Easing;
import io.wispforest.owo.braid.animation.Lerp;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Sized;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class AnimatedSized extends AutomaticallyAnimatedWidget {

    public final @Nullable Double width;
    public final @Nullable Double height;
    public final Widget child;

    public AnimatedSized(Duration duration, Easing easing, @Nullable Double width, @Nullable Double height, Widget child) {
        super(duration, easing);
        this.width = width;
        this.height = height;
        this.child = child;
    }

    @Override
    public State createState() {
        return new State();
    }

    public static class State extends AutomaticallyAnimatedWidget.State<AnimatedSized> {

        private Lerp<@Nullable Double> width;
        private Lerp<@Nullable Double> height;

        @Override
        protected void updateLerps() {
            this.width = this.visitNullableLerp(this.width, this.widget().width, DoubleLerp::new);
            this.height = this.visitNullableLerp(this.height, this.widget().height, DoubleLerp::new);
        }

        @Override
        public Widget build(BuildContext context) {
            return new Sized(
                this.width.compute(this.animationValue()),
                this.height.compute(this.animationValue()),
                this.widget().child
            );
        }
    }
}
