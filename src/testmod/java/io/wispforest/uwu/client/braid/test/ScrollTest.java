package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.animation.Easing;
import io.wispforest.owo.braid.core.BraidUtils;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.ListenableBuilder;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.drag.DragArena;
import io.wispforest.owo.braid.widgets.flex.*;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.scroll.*;
import io.wispforest.owo.braid.widgets.slider.slider.Slider;
import io.wispforest.owo.braid.widgets.window.Window;
import io.wispforest.owo.braid.widgets.window.WindowController;
import io.wispforest.owo.util.Wisdom;
import io.wispforest.uwu.client.braid.TestSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.time.Duration;

public class ScrollTest extends StatefulWidget {

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<ScrollTest> {

        private final ScrollController horizontalController = new ScrollController(this);
        private final ScrollController verticalController = new ScrollController(this);
        private final WindowController controller = new WindowController();

        private final ScrollController horizontalNestedScrollController = new ScrollController(this);
        private final ScrollController verticalNestedScrollController = new ScrollController(this);
        private final WindowController nestedScrollController = new WindowController();
        private double nestedSliderValue = 0.5;

        @Override
        public void init() {
            this.controller.setX((Minecraft.getInstance().getWindow().getGuiScaledWidth() - 200) / 2d);
            this.controller.setY((Minecraft.getInstance().getWindow().getGuiScaledHeight() - 200) / 2d);
        }

        @Override
        public Widget build(BuildContext context) {
            var text = BraidUtils.fold(
                Wisdom.ALL_THE_WISDOM,
                Component.empty(),
                (result, wisdom) -> {
                    var wisdomColor = Color.hsv(
                        new java.util.Random(wisdom.hashCode()).nextFloat(), .75f, 1f
                    ).rgb();

                    return result.append(
                        Component.literal(wisdom + "\n").withStyle(style -> style.withColor(wisdomColor))
                    );
                }
            );

            return new DragArena(
                new Window(
                    false,
                    Component.literal("wisdom, but colored!"),
                    null,
                    this.controller,
                    Size.square(200),
                    new Column(
                        new Flexible(
                            new Row(
                                new Flexible(
                                    new Scrollable(
                                        true,
                                        true,
                                        this.horizontalController,
                                        this.verticalController,
                                        new ScrollAnimationSettings(Duration.ofMillis(2000), Easing.OUT_QUART),
                                        new Sized(
                                            500.0,
                                            null,
                                            new Label(
                                                LabelStyle.SHADOW,
                                                true,
                                                text
                                            )
                                        )
                                    )
                                ),
                                new Sized(
                                    10.0,
                                    200.0,
                                    new ListenableBuilder(
                                        this.verticalController,
                                        buildContext -> new Slider(
                                            this.verticalController.offset(),
                                            widget -> widget
                                                .range(this.verticalController.maxOffset(), 0)
                                                .vertical(), this.verticalController::jumpTo
                                        )
                                    )
                                )
                            )
                        ),
                        new Sized(
                            null,
                            10.0,
                            new Row(
                                new Flexible(
                                    new ListenableBuilder(
                                        this.horizontalController,
                                        buildContext -> new Slider(
                                            this.horizontalController.offset(),
                                            widget -> widget
                                                .range(0, this.horizontalController.maxOffset()), this.horizontalController::jumpTo
                                        )
                                    )
                                ),
                                new Padding(Insets.all(5))
                            )
                        )
                    )
                ),
                new Window(
                    false,
                    Component.literal("Scrollception"),
                    null,
                    this.nestedScrollController,
                    Size.square(200),
                    new Column(
                        Label.literal("Damn bro, you can scroll this?"),
                        new Flexible(
                            new ScrollableWithBars(
                                horizontalNestedScrollController,
                                verticalNestedScrollController,
                                ScrollAnimationSettings.DEFAULT,
                                10,
                                ButtonScrollbar::new,
                                new Sized(
                                    500, 500,
                                    new Center(
                                        new Column(
                                            MainAxisAlignment.CENTER,
                                            CrossAxisAlignment.CENTER,
                                            new Sized(
                                                100, 20,
                                                new Slider(
                                                    this.nestedSliderValue,
                                                    null, value -> this.setState(() -> this.nestedSliderValue = value)
                                                )
                                            ),
                                            new Sized(
                                                20, 100,
                                                new Slider(
                                                    this.nestedSliderValue,
                                                    Slider::vertical, value -> this.setState(() -> this.nestedSliderValue = value)
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
}
