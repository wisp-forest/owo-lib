package io.wispforest.owo.braid.widgets.animated;

import io.wispforest.owo.braid.animation.AutomaticallyAnimatedWidget;
import io.wispforest.owo.braid.animation.ColorLerp;
import io.wispforest.owo.braid.animation.Easing;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class AnimatedBox extends AutomaticallyAnimatedWidget {

    public final Color color;
    public final boolean outline;
    public final @Nullable Widget child;

    public AnimatedBox(Duration duration, Easing easing, Color color, boolean outline, @Nullable Widget child) {
        super(duration, easing);
        this.color = color;
        this.outline = outline;
        this.child = child;
    }

    public AnimatedBox(Duration duration, Easing easing, Color color, boolean outline) {
        this(duration, easing, color, outline, null);
    }

    public AnimatedBox(Duration duration, Easing easing, Color color) {
        this(duration, easing, color, false);
    }

    @Override
    public State createState() {
        return new State();
    }

    public static class State extends AutomaticallyAnimatedWidget.State<AnimatedBox> {

        private ColorLerp color;

        @Override
        protected void updateLerps() {
            this.color = this.visitLerp(this.color, this.widget().color, ColorLerp::new);
        }

        @Override
        public Widget build(BuildContext context) {
            return new Box(
                this.color.compute(this.animationValue()),
                this.widget().outline,
                this.widget().child
            );
        }
    }
}
