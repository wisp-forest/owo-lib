package io.wispforest.uwu.client.braid;

import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.scroll.VerticallyScrollable;
import io.wispforest.owo.braid.widgets.slider.SliderStyle;
import io.wispforest.owo.braid.widgets.slider.drag.MessageDrag;
import io.wispforest.owo.braid.widgets.slider.range.DefaultRangeSliderStyle;
import io.wispforest.owo.braid.widgets.slider.range.MessageRangeSlider;
import io.wispforest.owo.braid.widgets.slider.range.RangeSliderStyle;
import io.wispforest.owo.braid.widgets.slider.slider.MessageSlider;
import io.wispforest.owo.braid.widgets.slider.slider.Slider;
import io.wispforest.owo.braid.widgets.slider.slider.SliderFunction;
import io.wispforest.owo.braid.widgets.slider.xlyder.DefaultXlyderStyle;
import io.wispforest.owo.braid.widgets.slider.xlyder.MessageXlyder;
import io.wispforest.owo.braid.widgets.slider.xlyder.Xlyder;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.uwu.client.Bikeshed;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SliderTests extends StatefulWidget {

    public enum SliderTest {
        BASIC, DIRECTION, REDUNDANT, SLIDER
    }

    @Override
    public WidgetState<SliderTests> createState() {
        return new State();
    }

    public static class State extends WidgetState<SliderTests> {

        private SliderTest test = SliderTest.BASIC;

        @Override
        public Widget build(BuildContext context) {
            return new Stack(
                Alignment.CENTER,
                new DefaultXlyderStyle(
                    new SliderStyle<>(
                        null,
                        active -> new HoverableBuilder((hoverableContext, hovered) -> {
                            return hovered ? new TestSelector.GayAmogus(4) : new Box(Color.WHITE);
                        }),
                        Size.of(16, 16),
                        null
                    ),
                    new DefaultRangeSliderStyle(
                        new RangeSliderStyle(null, new Bikeshed(), null, null, null, null, Optional.of(SoundEvents.ANVIL_LAND)),
                        switch (this.test) {
                            case BASIC -> new BasicSliderTest();
                            case DIRECTION -> new SliderDirectionTest();
                            case REDUNDANT -> new IncrediblyRedundantSlider();
                            case SLIDER -> new NormalSliderTest();
                        }
                    )
                ),
                new Align(
                    Alignment.TOP_RIGHT,
                    new HitTestTrap(
                        new Padding(
                            Insets.all(5),
                            new Panel(
                                OwoUIGraphics.PANEL_NINE_PATCH_TEXTURE,
                                new Padding(
                                    Insets.all(5),
                                    new Sized(
                                        null, 66,
                                        new VerticallyScrollable(
                                            new IntrinsicWidth(
                                                new Column(
                                                    MainAxisAlignment.START,
                                                    CrossAxisAlignment.CENTER,
                                                    new Padding(Insets.all(2)),
                                                    Stream.of(SliderTest.values()).map(test -> new MessageButton(
                                                        Component.literal(test.name().toLowerCase(Locale.ROOT).replace('_', ' ')),
                                                        test != this.test ? () -> this.setState(() -> this.test = test) : null
                                                    )).collect(Collectors.toList())
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            );
        }
    }

    public static String formatDouble(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString().replaceAll("(\\.0*|(?<=\\d)\\.0+)$", "");
    }


    public static class BasicSliderTest extends StatefulWidget {

        @Override
        public WidgetState<BasicSliderTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<BasicSliderTest> {
            private double discreteX = 16;
            private double discreteY = 16;
            private double smoothX = 16;
            private double smoothY = 16;

            @Override
            public Widget build(BuildContext context) {
                return new Grid(
                    LayoutAxis.VERTICAL,
                    3,
                    Grid.CellFit.tight(),
                    widget -> new Padding(Insets.all(5), widget),
                    null,
                    Label.literal("Discrete"),
                    Label.literal("Smooth"),
                    Label.literal("Basic"),
                    new Sized(
                        100, 20,
                        new MessageSlider(
                            discreteX,
                            Component.literal("v: " + formatDouble(discreteX)), widget -> widget
                                .range(0, 32)
                                .step(2),
                            newValue -> this.setState(() -> this.discreteX = newValue)
                        )
                    ),
                    new Sized(
                        100, 20,
                        new MessageSlider(
                            smoothX,
                            Component.literal("v: " + formatDouble(smoothX)), widget -> widget.range(0, 32),
                            newValue -> this.setState(() -> this.smoothX = newValue)
                        )
                    ),
                    Label.literal("XY"),
                    new Sized(
                        100, 100,
                        new MessageXlyder(
                            discreteX, discreteY,
                            Component.literal("x: " + formatDouble(discreteX) + "\ny: " + formatDouble(discreteY)),
                            xlyder -> xlyder
                                .range(0, 32)
                                .step(2),
                            (x, y) -> this.setState(() -> {
                                this.discreteX = x;
                                this.discreteY = y;
                            })
                        )
                    ),
                    new Sized(
                        100, 100,
                        new MessageXlyder(
                            smoothX, smoothY,
                            Component.literal("x: " + formatDouble(smoothX) + "\ny: " + formatDouble(smoothY)),
                            xlyder -> xlyder.range(0, 32),
                            (x, y) -> this.setState(() -> {
                                this.smoothX = x;
                                this.smoothY = y;
                            })
                        )
                    ),
                    Label.literal("Range"),
                    new Sized(
                        100, 20,
                        new MessageRangeSlider(
                            discreteX, discreteY,
                            Component.literal("v: " + formatDouble(discreteX) + "-" + formatDouble(discreteY)), slider -> slider
                                .range(0, 32)
                                .step(2),
                            (min, max) -> this.setState(() -> {
                                this.discreteX = min;
                                this.discreteY = max;
                            })
                        )
                    ),
                    new Sized(
                        100, 20,
                        new MessageRangeSlider(
                            smoothX, smoothY,
                            Component.literal("v: " + formatDouble(smoothX) + "-" + formatDouble(smoothY)), slider -> slider.range(0, 32),
                            (min, max) -> this.setState(() -> {
                                this.smoothX = min;
                                this.smoothY = max;
                            })
                        )
                    ),
                    Label.literal("Drag"),
                    new Sized(
                        100, 20,
                        new MessageDrag(
                            discreteX,
                            drag -> drag.range(0, 32).step(2),
                            newValue -> this.setState(() -> this.discreteX = newValue),
                            Component.literal("v: " + formatDouble(discreteX))
                        )
                    ),
                    new Sized(
                        100, 20,
                        new MessageDrag(
                            smoothX,
                            drag -> drag.range(0, 32),
                            newValue -> this.setState(() -> this.smoothX = newValue),
                            Component.literal("v: " + formatDouble(smoothX))
                        )
                    )
                );
            }
        }
    }

    public static class NormalSliderTest extends StatefulWidget {

        @Override
        public WidgetState<NormalSliderTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<NormalSliderTest> {
            private double value = 16;

            @Override
            public Widget build(BuildContext context) {
                return new Grid(
                    LayoutAxis.VERTICAL,
                    3,
                    Grid.CellFit.tight(),
                    widget -> new Padding(Insets.all(5), widget),
                    null,
                    Label.literal("Discrete"),
                    Label.literal("Smooth"),
                    Label.literal("Linear"),
                    slider(widget -> widget.range(0, 32).step(2)),
                    slider(widget -> widget.range(0, 32)),
                    Label.literal("Logarithmic"),
                    slider(widget -> widget.range(0, 32).step(2).function(SliderFunction.LOGARITHMIC)),
                    slider(widget -> widget.range(0, 32).function(SliderFunction.LOGARITHMIC)),
                    Label.literal("Reverse"),
                    slider(widget -> widget.range(32, 0).step(2)),
                    slider(widget -> widget.range(32, 0)),
                    Label.literal("Logarithmic Reverse"),
                    slider(widget -> widget.range(32, 0).step(2).function(SliderFunction.LOGARITHMIC)),
                    slider(widget -> widget.range(32, 0).function(SliderFunction.LOGARITHMIC))
                );
            }

            private Widget slider(WidgetSetupCallback<Slider> setupCallback) {
                return new Sized(
                    100, 20,
                    new MessageSlider(
                        value,
                        Component.literal("v: " + formatDouble(value)), setupCallback,
                        newValue -> this.setState(() -> this.value = newValue)
                    )
                );
            }
        }
    }


    public static class SliderDirectionTest extends StatefulWidget {
        @Override
        public WidgetState<SliderDirectionTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<SliderDirectionTest> {

            private static final double min = 0, max = 32;

            private double x = 16;
            private double y = 16;

            @Override
            public Widget build(BuildContext context) {
                return new Grid(
                    LayoutAxis.VERTICAL,
                    3,
                    Grid.CellFit.tight(),
                    new Sized(
                        100, 100,
                        new Xlyder(
                            x, y,
                            xlyder -> xlyder.rangeX(max, min).rangeY(min, max).incrementStep(1), (x, y) -> this.setState(() -> {
                            this.x = x;
                            this.y = y;
                        })
                        )
                    ),
                    new Sized(
                        20, 100,
                        new Slider(
                            y,
                            slider -> slider.range(min, max).vertical().incrementStep(1), newValue -> this.setState(() -> this.y = newValue)
                        )
                    ),
                    new Sized(
                        100, 100,
                        new Xlyder(
                            x, y,
                            xlyder -> xlyder.range(min, max).incrementStep(1), (x, y) -> this.setState(() -> {
                            this.x = x;
                            this.y = y;
                        })
                        )
                    ),
                    new Sized(
                        100, 20,
                        new Slider(
                            x,
                            slider -> slider.range(max, min).incrementStep(1), newValue -> this.setState(() -> this.x = newValue)
                        )
                    ),
                    new Sized(
                        20, 20,
                        new Transform(
                            new Matrix3x2f().scale(0.5f),
                            new Label(
                                null,
                                false,
                                Label.Overflow.SHOW,
                                Component.literal(formatDouble(x) + "\n" + formatDouble(y))
                            )
                        )
                    ),
                    new Sized(
                        100, 20,
                        new Slider(
                            x,
                            slider -> slider.range(min, max).incrementStep(1), newValue -> this.setState(() -> this.x = newValue)
                        )
                    ),
                    new Sized(
                        100, 100,
                        new Xlyder(
                            x, y,
                            xlyder -> xlyder.range(max, min).incrementStep(1), (x, y) -> this.setState(() -> {
                            this.x = x;
                            this.y = y;
                        })
                        )
                    ),
                    new Sized(
                        20, 100,
                        new Slider(
                            y,
                            slider -> slider.range(max, min).vertical().incrementStep(1), newValue -> this.setState(() -> this.y = newValue)
                        )
                    ),
                    new Sized(
                        100, 100,
                        new Xlyder(
                            x, y,
                            xlyder -> xlyder.rangeX(min, max).rangeY(max, min).incrementStep(1), (x, y) -> this.setState(() -> {
                            this.x = x;
                            this.y = y;
                        })
                        )
                    )
                );
            }
        }
    }

    public static class IncrediblyRedundantSlider extends StatefulWidget {
        @Override
        public WidgetState<IncrediblyRedundantSlider> createState() {
            return new State();
        }

        public static class State extends WidgetState<IncrediblyRedundantSlider> {

            private double x, y;

            @Override
            public Widget build(BuildContext context) {
                return new Column(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new Padding(
                        Insets.all(20),
                        new Label(Component.literal("incredibly redundant slider™"))
                    ),
                    new Sized(
                        100.0,
                        15.0,
                        new Slider(
                            this.x,
                            null, (x) -> this.setState(() -> this.x = x)
                        )
                    ),
                    new Row(
                        new Sized(
                            15.0,
                            100.0,
                            new Slider(
                                this.y,
                                Slider::vertical, (y) -> this.setState(() -> this.y = y)
                            )
                        ),
                        new Sized(
                            100.0,
                            100.0,
                            new Xlyder(
                                this.x, this.y,
                                xlyder -> xlyder.style(new SliderStyle<>(null, null, Size.square((1 - this.y) * 16 + 8), null)), (x, y) -> this.setState(() -> {
                                this.x = x;
                                this.y = y;
                            })
                            )
                        ),
                        new Sized(
                            15.0,
                            100.0,
                            new Slider(
                                this.y,
                                Slider::vertical, y -> this.setState(() -> this.y = y)
                            )
                        )
                    ),
                    new Sized(
                        100.0,
                        15.0,
                        new Slider(
                            this.x,
                            slider -> slider.style(new SliderStyle<>(null, null, 24.0, null)), x -> this.setState(() -> this.x = x)
                        )
                    ),
                    new Sized(
                        100.0,
                        15.0,
                        new Slider(
                            this.x,
                            slider -> slider.style(new SliderStyle<>(null, null, 18.0, Optional.empty())), x -> this.setState(() -> this.x = x)
                        )
                    ),
                    new Sized(
                        100.0,
                        15.0,
                        new Slider(
                            this.x,
                            slider -> slider.style(new SliderStyle<>(null, null, 12.0, null)), x -> this.setState(() -> this.x = x)
                        )
                    ),
                    new Sized(
                        100.0,
                        15.0,
                        new Slider(
                            this.x,
                            slider -> slider.style(new SliderStyle<>(null, null, 6.0, null)), (x) -> this.setState(() -> this.x = x)
                        )
                    )
                );
            }
        }
    }
}
