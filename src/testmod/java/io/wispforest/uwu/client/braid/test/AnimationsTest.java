package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.animation.AlignmentLerp;
import io.wispforest.owo.braid.animation.Animation;
import io.wispforest.owo.braid.animation.Easing;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.animated.AnimatedAlign;
import io.wispforest.owo.braid.widgets.animated.AnimatedBox;
import io.wispforest.owo.braid.widgets.animated.AnimatedPadding;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.uwu.client.braid.Amogus;
import net.minecraft.network.chat.Component;

import java.time.Duration;

public class AnimationsTest extends StatefulWidget {
    @Override
    public WidgetState<AnimationsTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<AnimationsTest> {

        private boolean end = false;

        @Override
        public Widget build(BuildContext context) {
            return new Row(
                MainAxisAlignment.START,
                CrossAxisAlignment.CENTER,
                new Sized(
                    250,
                    250,
                    new Align(
                        Alignment.TOP,
                        new Column(
                            new AnimatedAlign(
                                Duration.ofMillis(250),
                                Easing.IN_OUT_EXPO,
                                this.end ? Alignment.RIGHT : Alignment.CENTER,
                                new MessageButton(Component.literal("toggle"), () -> this.setState(() -> this.end = !this.end))
                            ),
                            new Box(
                                Color.WHITE,
                                new AnimatedPadding(
                                    Duration.ofMillis(500),
                                    Easing.IN_OUT_EXPO,
                                    this.end ? Insets.of(0, 50, 50, 50) : Insets.none(),
                                    new Sized(
                                        30,
                                        30,
                                        new AnimatedBox(
                                            Duration.ofMillis(500),
                                            Easing.IN_OUT_QUAD,
                                            this.end ? Color.RED : Color.GREEN
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                new ManualAnimation(
                    new Amogus(
                        new Box(Color.BLUE),
                        new Box(Color.WHITE),
                        8
                    )
                )
            );
        }
    }

    public static class ManualAnimation extends StatefulWidget {

        public final Widget child;

        public ManualAnimation(Widget child) {
            this.child = child;
        }

        @Override
        public WidgetState<ManualAnimation> createState() {
            return new ManualAnimation.State();
        }

        public static class State extends WidgetState<ManualAnimation> {

            private static final AlignmentLerp ALIGNMENT_LERP = new AlignmentLerp(Alignment.LEFT, Alignment.RIGHT);

            private Animation.Target currentTarget;
            private Animation animation;
            private Alignment alignment;

            @Override
            public void init() {
                this.currentTarget = Animation.Target.START;
                this.animation = new Animation(
                    Easing.OUT_BOUNCE,
                    Duration.ofMillis(500),
                    this::scheduleAnimationCallback,
                    this::onAnimationTick,
                    this.currentTarget
                );

                this.onAnimationTick(this.animation.progress());
            }

            private void onAnimationTick(double progress) {
                this.setState(() -> this.alignment = ALIGNMENT_LERP.compute(progress));
            }

            @Override
            public Widget build(BuildContext context) {
                return new Column(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new Sized(
                        128,
                        null,
                        new Box(
                            Color.WHITE.withA(.5),
                            true,
                            new Padding(
                                Insets.all(1),
                                new Align(
                                    this.alignment,
                                    this.widget().child
                                )
                            )
                        )
                    ),
                    new Padding(Insets.vertical(5)),
                    new MessageButton(
                        Component.literal("toggle"),
                        () -> {
                            var next = this.currentTarget == Animation.Target.START ? Animation.Target.END : Animation.Target.START;
                            this.currentTarget = next;

                            this.animation.towards(next, false);
                        }
                    )
                );
            }
        }
    }
}
