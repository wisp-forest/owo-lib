package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.animation.AutomaticallyAnimatedWidget;
import io.wispforest.owo.braid.animation.DoubleLerp;
import io.wispforest.owo.braid.animation.Easing;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.BraidLogo;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.basic.Constrain;
import io.wispforest.owo.braid.widgets.basic.LayoutBuilder;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.drag.DragArena;
import io.wispforest.owo.braid.widgets.drag.DragArenaElement;
import io.wispforest.uwu.client.braid.TestSelector;

import java.time.Duration;

public class AutomaticAnimationTest extends StatefulWidget {
    @Override
    public WidgetState<AutomaticAnimationTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<AutomaticAnimationTest> {

        private double x = 0;
        private double y = 0;

        @Override
        public Widget build(BuildContext context) {
            return new MouseArea(
                widget -> widget
                    .clickCallback((toX, toY, button, mods) -> {
                        this.setState(() -> {
                            this.x = toX;
                            this.y = toY;
                        });

                        return true;
                    }).dragCallback((x, y, dx, dy) -> this.setState(() -> {
                        this.x = x;
                        this.y = y;
                    })),
                new LayoutBuilder((context1, constraints) -> {
                    System.out.println("layout rebuild");
                    return new Constrain(
                        Constraints.of(constraints.maxWidth(), constraints.maxHeight(), constraints.maxWidth(), constraints.maxHeight()),
                        new DragArena(
                            new TheWidget(
                                Duration.ofMillis(250),
                                Easing.OUT_EXPO,
                                this.x,
                                this.y
                            )
                        )
                    );
                })
            );
        }
    }

    public static class TheWidget extends AutomaticallyAnimatedWidget {

        public final double x;
        public final double y;

        public TheWidget(Duration duration, Easing easing, double x, double y) {
            super(duration, easing);
            this.x = x;
            this.y = y;
        }

        @Override
        public TheWidget.State createState() {
            return new TheWidget.State();
        }

        public static class State extends AutomaticallyAnimatedWidget.State<TheWidget> {

            private DoubleLerp x;
            private DoubleLerp y;

            @Override
            protected void updateLerps() {
                this.x = this.visitLerp(this.x, this.widget().x, DoubleLerp::new);
                this.y = this.visitLerp(this.y, this.widget().y, DoubleLerp::new);
            }

            @Override
            public Widget build(BuildContext context) {
                return new DragArenaElement(
                    this.x.compute(this.animationValue()),
                    this.y.compute(this.animationValue()),
                    new Align(
                        Alignment.of(-.5, -.5),
                        2d, 2d,
                        new BraidLogo()
                    )
                );
            }
        }
    }
}
