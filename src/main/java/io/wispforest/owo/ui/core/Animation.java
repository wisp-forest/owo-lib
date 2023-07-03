package io.wispforest.owo.ui.core;

import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Animation<A extends Animatable<A>> {

    private final int duration;

    private float delta = 0;
    private Direction direction = Direction.BACKWARDS;
    private boolean looping = false;

    private final Consumer<A> setter;
    private final Easing easing;

    private final A from;
    private final A to;

    private final EventStream<Finished> finishedEvents = Finished.newStream();
    private boolean eventInvoked = true;

    public Animation(int duration, Consumer<A> setter, Easing easing, A from, A to) {
        this.duration = duration;
        this.setter = setter;
        this.easing = easing;
        this.from = from;
        this.to = to;
    }

    public static Composed compose(Animation<?>... elements) {
        return new Composed(elements);
    }

    public void update(float delta) {
        if (this.delta == this.direction.targetDelta) {
            if (!this.eventInvoked) {
                this.finishedEvents.sink().onFinished(this.direction, this.looping);
                this.eventInvoked = true;
            }

            if (this.looping) this.reverse();
            else return;
        }

        this.delta = MathHelper.clamp(this.delta + (delta * 50 / duration) * this.direction.multiplier, 0, 1);

        this.setter.accept(this.from.interpolate(this.to, this.easing.apply(this.delta)));
    }

    public Animation<A> forwards() {
        this.setDirection(Direction.FORWARDS);
        return this;
    }

    public Animation<A> backwards() {
        this.setDirection(Direction.BACKWARDS);
        return this;
    }

    public Animation<A> reverse() {
        this.setDirection(this.direction.reversed());
        return this;
    }

    private void setDirection(Direction direction) {
        if (this.direction == direction) return;
        this.direction = direction;
        this.eventInvoked = false;
    }

    public Animation<A> loop(boolean loop) {
        this.looping = loop;
        return this;
    }

    public boolean looping() {
        return this.looping;
    }

    public Direction direction() {
        return this.direction;
    }

    public EventSource<Finished> finished() {
        return this.finishedEvents.source();
    }

    public enum Direction {
        FORWARDS(1, 1),
        BACKWARDS(-1, 0);

        public final int multiplier;
        public final float targetDelta;

        Direction(int multiplier, float targetDelta) {
            this.multiplier = multiplier;
            this.targetDelta = targetDelta;
        }

        public Direction reversed() {
            return switch (this) {
                case FORWARDS -> BACKWARDS;
                case BACKWARDS -> FORWARDS;
            };
        }
    }

    public interface Finished {
        void onFinished(Direction direction, boolean looping);

        static EventStream<Finished> newStream() {
            return new EventStream<>(subscribers -> (direction, looping) -> {
                for (var subscriber : subscribers) {
                    subscriber.onFinished(direction, looping);
                }
            });
        }
    }

    public static class Composed {
        private final List<Animation<?>> elements;

        private Composed(Animation<?>... elements) {
            this.elements = Arrays.asList(elements);
        }

        public void forwards() {
            this.elements.forEach(Animation::forwards);
        }

        public void backwards() {
            this.elements.forEach(Animation::backwards);
        }

        public void reverse() {
            this.elements.forEach(Animation::reverse);
        }

        public void loop(boolean loop) {
            this.elements.forEach(animation -> animation.loop(loop));
        }
    }

}
