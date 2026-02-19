package io.wispforest.owo.braid.animation;

import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;

public abstract class AutomaticallyAnimatedWidget extends StatefulWidget {

    private static final Logger log = LoggerFactory.getLogger(AutomaticallyAnimatedWidget.class);
    public final Duration duration;
    public final Easing easing;

    protected AutomaticallyAnimatedWidget(Duration duration, Easing easing) {
        this.duration = duration;
        this.easing = easing;
    }

    @Override
    public abstract State<?> createState();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static abstract class State<T extends AutomaticallyAnimatedWidget> extends WidgetState<T> {

        private Animation animation;
        private LerpVisitor activeVisitor;

        private void callback(double progress) {
            this.setState(() -> {});
        }

        @Override
        public void init() {
            this.animation = new Animation(
                this.widget().easing,
                this.widget().duration,
                this::scheduleAnimationCallback,
                this::callback,
                Animation.Target.END
            );

            this.visitLerps((previous, targetValue, factory) -> {
                return factory.make(targetValue, targetValue);
            });
        }

        @Override
        public void didUpdateWidget(AutomaticallyAnimatedWidget oldWidget) {
            var restartAnimation = new MutableBoolean(this.widget().easing != oldWidget.easing);
            this.animation.duration = this.widget().duration;

            if (restartAnimation.isFalse()) {
                this.visitLerps((previous, targetValue, factory) -> {
                    if (!Objects.equals(previous.end, targetValue)) {
                        restartAnimation.setTrue();
                    }

                    return previous;
                });
            }

            if (restartAnimation.isTrue()) {
                this.visitLerps((previous, targetValue, factory) -> factory.make(previous.compute(this.animationValue()), targetValue));
                this.animation.easing = this.widget().easing;
                this.animation.towards(Animation.Target.END);
            }
        }

        private void visitLerps(LerpVisitor visitor) {
            this.activeVisitor = visitor;
            this.updateLerps();
        }

        // ---

        protected double animationValue() {
            return this.animation.progress();
        }

        protected <L extends Lerp<V>, V> L visitLerp(@Nullable Lerp<V> previous, V targetValue, Lerp.Factory<L, V> factory) {
            return (L) this.activeVisitor.visit(previous, targetValue, factory);
        }

        protected <L extends Lerp<V>, V> L visitNullableLerp(@Nullable Lerp<V> previous, V targetValue, Lerp.Factory<L, V> factory) {
            return (L) this.activeVisitor.visit(previous, targetValue, (start, end) -> new NullableLerp(start, end, factory));
        }

        protected abstract void updateLerps();
    }

    @FunctionalInterface
    private interface LerpVisitor<L extends Lerp<V>, V> {
        L visit(@Nullable Lerp<V> previous, V targetValue, Lerp.Factory<L, V> factory);
    }
}
