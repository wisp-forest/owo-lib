package io.wispforest.uwu.client.braid;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.Button;
import io.wispforest.owo.braid.widgets.ItemStackWidget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.drag.DragArena;
import io.wispforest.owo.braid.widgets.drag.DragArenaElement;
import io.wispforest.owo.braid.widgets.flex.*;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.slider.Slider;
import io.wispforest.owo.braid.widgets.splitpane.SplitPane;
import io.wispforest.owo.braid.widgets.window.Window;
import io.wispforest.owo.braid.widgets.window.WindowController;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleFunction;

public class TestSelector extends StatefulWidget {

    public enum Tests {
        COUNTER, FLEX, DRAGGING, SPLIT_PANE
    }

    @Override
    public WidgetState<TestSelector> createState() {
        return new State();
    }

    public static class State extends WidgetState<TestSelector> {

        private Tests test = Tests.COUNTER;

        @Override
        public Widget build(BuildContext context) {
            return new Stack(
                Alignment.CENTER,
                new Center(
                    switch (this.test) {
                        case COUNTER -> new Counter();
                        case FLEX -> new FunnySwitchLayout();
                        case DRAGGING -> new DragArenaTest();
                        case SPLIT_PANE -> new SplitPaneTest();
                    }
                ),
                new Align(
                    Alignment.LEFT,
                    new HitTestTrap(
                        new Padding(
                            Insets.left(5),
                            new Panel(
                                OwoUIDrawContext.PANEL_NINE_PATCH_TEXTURE,
                                new Padding(
                                    Insets.all(8),
                                    new Sized(
                                        65.0,
                                        null,
                                        new Column(
                                            new Button(Text.literal("counter"), () -> setState(() -> this.test = Tests.COUNTER)),
                                            new Padding(Insets.all(2)),
                                            new Button(Text.literal("flex"), () -> setState(() -> this.test = Tests.FLEX)),
                                            new Padding(Insets.all(2)),
                                            new Button(Text.literal("dragging"), () -> setState(() -> this.test = Tests.DRAGGING)),
                                            new Padding(Insets.all(2)),
                                            new Button(Text.literal("split pane"), () -> setState(() -> this.test = Tests.SPLIT_PANE))
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


    public static class Counter extends StatefulWidget {
        @Override
        public WidgetState<Counter> createState() {
            return new State();
        }

        public static class State extends WidgetState<Counter> {
            private int count = 0;

            @Override
            public Widget build(BuildContext context) {
                return new Sized(
                    50.0,
                    null,
                    new Column(
                        new Label(
                            LabelStyle.DEFAULT,
                            false,
                            Text.literal("count: " + this.count)
                        ),
                        new Row(
                            new Flexible(
                                new Button(
                                    Text.literal("+"),
                                    () -> this.setState(() -> this.count++)
                                )
                            ),
                            new Flexible(
                                new Button(
                                    Text.literal("-"),
                                    () -> this.setState(() -> this.count--)
                                )
                            )
                        )
                    )
                );
            }
        }
    }

    public static class FunnySwitchLayout extends StatefulWidget {
        @Override
        public WidgetState<FunnySwitchLayout> createState() {
            return new State();
        }

        private static class State extends WidgetState<FunnySwitchLayout> {
            private LayoutAxis axis = LayoutAxis.HORIZONTAL;

            @Override
            public Widget build(BuildContext context) {
                return new Flex(
                    this.axis,
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new Button(
                        Text.literal("switch axis"),
                        () -> this.setState(() -> this.axis = this.axis.opposite())
                    ),
                    new Padding(Insets.all(5)),
                    new Panel(
                        OwoUIDrawContext.PANEL_NINE_PATCH_TEXTURE,
                        new Padding(
                            Insets.all(10),
                            new Column(
                                MainAxisAlignment.START,
                                CrossAxisAlignment.CENTER,
                                new Label(Text.literal("that's text")),
                                new Label(Text.literal("some more text")),
                                new Padding(
                                    Insets.top(5),
                                    new Counter()
                                )
                            )
                        )
                    ),
                    new Padding(Insets.all(5)),
                    new Panel(
                        OwoUIDrawContext.DARK_PANEL_NINE_PATCH_TEXTURE,
                        new Padding(
                            Insets.all(10),
                            new Column(
                                MainAxisAlignment.START,
                                CrossAxisAlignment.CENTER,
                                new Label(Text.literal("that's text")),
                                new Label(Text.literal("some more text")),
                                new Padding(
                                    Insets.top(5),
                                    new Counter()
                                )
                            )
                        )
                    )
                );
            }
        }
    }

    public static class DragArenaTest extends StatefulWidget {
        @Override
        public WidgetState<DragArenaTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<DragArenaTest> {

            private final Set<WindowController> windows = new HashSet<>();

            @Override
            public Widget build(BuildContext context) {
                var elements = new ArrayList<Widget>(List.of(
                    new FunnyDragText(Text.literal("me too!")),
                    new FunnyDragText(Text.literal("drag me!"))
                ));

                for (var controller : this.windows) {
                    elements.add(new Window(
                        true,
                        Text.literal("window " + controller.hashCode()),
                        () -> setState(() -> this.windows.remove(controller)),
                        controller,
                        new Stack(
                            new Align(
                                Alignment.TOP_LEFT,
                                new Column(
                                    new Label(Text.literal("a")),
                                    new Button(Text.literal("window button :o"), () -> setState(() -> controller.expanded = !controller.expanded))
                                )
                            ),
                            new Align(
                                Alignment.BOTTOM_RIGHT,
                                new Tooltip(
                                    Text.literal("tooltip\nhere?"),
                                    new ItemStackWidget(
                                        Registries.ITEM.getRandom(Random.create(controller.hashCode())).get().value().getDefaultStack(),
                                        false
                                    )
                                )
                            )
                        )
                    ));
                }

                return new Stack(
                    new DragArena(elements),
                    new Align(
                        Alignment.BOTTOM,
                        new Button(
                            Text.literal("add window"),
                            () -> setState(() -> this.windows.add(new WindowController(Size.of(150, 75))))
                        )
                    )
                );
            }
        }
    }

    public static class FunnyDragText extends StatefulWidget {

        public final Text text;

        public FunnyDragText(Text text) {
            this.text = text;
        }

        @Override
        public WidgetState<FunnyDragText> createState() {
            return new State();
        }

        public static class State extends WidgetState<FunnyDragText> {

            private double x = 0, y = 0;

            @Override
            public Widget build(BuildContext context) {
                return new DragArenaElement(
                    this.x,
                    this.y,
                    new MouseArea(
                        widget -> widget
                            .dragCallback(($, $$, dx, dy) -> this.setState(() -> {
                                this.x += dx;
                                this.y += dy;
                            }))
                            .cursorStyle(CursorStyle.HAND),
                        new Panel(
                            OwoUIDrawContext.DARK_PANEL_NINE_PATCH_TEXTURE,
                            new Padding(
                                Insets.all(5),
                                new Label(this.widget().text)
                            )
                        )
                    )
                );
            }
        }
    }

    public static class SplitPaneTest extends StatelessWidget {
        @Override
        public Widget build(BuildContext context) {
            return new Column(
                MainAxisAlignment.START,
                CrossAxisAlignment.CENTER,
                new Sized(
                    100.0,
                    50.0,
                    new SplitPane(
                        new Box(
                            Color.BLACK.interpolate(Color.ofArgb(0), .5f),
                            new Label(Text.literal("text here"))
                        ),
                        new Box(
                            Color.BLACK.interpolate(Color.ofArgb(0), .5f),
                            new Label(Text.literal("more text here"))
                        ),
                        LayoutAxis.HORIZONTAL
                    )
                ),
                new Padding(Insets.all(10)),
                new SliderWithText(2.0, value -> Text.literal("value: " + BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString())),
                new Padding(Insets.all(10)),
                new SliderWithText(null, value -> Text.literal("value: " + BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString()))
            );
        }
    }

    public static class SliderWithText extends StatefulWidget {

        public final @Nullable Double step;
        public final DoubleFunction<Text> textSupplier;

        public SliderWithText(@Nullable Double step, DoubleFunction<Text> textSupplier) {
            this.step = step;
            this.textSupplier = textSupplier;
        }

        @Override
        public WidgetState<SliderWithText> createState() {
            return new State();
        }

        public static class State extends WidgetState<SliderWithText> {

            private double value = 16;

            @Override
            public Widget build(BuildContext context) {
                return new Column(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new Sized(
                        100.0,
                        20.0,
                        new Slider(this.value, 0, 32, this.widget().step, newValue -> setState(() -> this.value = newValue))
                    ),
                    new Padding(Insets.all(5)),
                    new Label(this.widget().textSupplier.apply(this.value))
                );
            }
        }
    }
}
