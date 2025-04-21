package io.wispforest.uwu.client.braid;

import com.mojang.authlib.GameProfile;
import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.Button;
import io.wispforest.owo.braid.widgets.EntityWidget;
import io.wispforest.owo.braid.widgets.ItemStackWidget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.basic.Stack;
import io.wispforest.owo.braid.widgets.drag.DragArena;
import io.wispforest.owo.braid.widgets.drag.DragArenaElement;
import io.wispforest.owo.braid.widgets.flex.*;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.scroll.ScrollController;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import io.wispforest.owo.braid.widgets.slider.MessageSlider;
import io.wispforest.owo.braid.widgets.slider.Slider;
import io.wispforest.owo.braid.widgets.splitpane.SplitPane;
import io.wispforest.owo.braid.widgets.textinput.TextBox;
import io.wispforest.owo.braid.widgets.textinput.TextEditingController;
import io.wispforest.owo.braid.widgets.window.Window;
import io.wispforest.owo.braid.widgets.window.WindowController;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.Wisdom;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.DoubleFunction;

public class TestSelector extends StatefulWidget {

    public enum Tests {
        COUNTER, FLEX, DRAGGING, SPLIT_PANE, TEXT_INPUT, BURNING_CHYZ, SCROLLING
    }

    @Override
    public WidgetState<TestSelector> createState() {
        return new State();
    }

    public static class State extends WidgetState<TestSelector> {

        private Tests test = Tests.TEXT_INPUT;
        private Entity chyz;

        @Override
        public void init() {
            this.chyz = EntityComponent.createRenderablePlayer(new GameProfile(
                UUID.fromString("09de8a6d-86bf-4c15-bb93-ce3384ce4e96"),
                "chyzman"
            ));

            this.chyz.setOnFire(true);
        }

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
                        case TEXT_INPUT -> new TextInputTest();
                        case BURNING_CHYZ -> new BurningChyzTest(this.chyz);
                        case SCROLLING -> new ScrollTest();
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
                                            new Button(Text.literal("split pane"), () -> setState(() -> this.test = Tests.SPLIT_PANE)),
                                            new Padding(Insets.all(2)),
                                            new Button(Text.literal("text input"), () -> setState(() -> this.test = Tests.TEXT_INPUT)),
                                            new Padding(Insets.all(2)),
                                            new BurningChyzButton(this.chyz, () -> setState(() -> this.test = Tests.BURNING_CHYZ)),
                                            new Padding(Insets.all(2)),
                                            new Button(Text.literal("scrolling"), () -> setState(() -> this.test = Tests.SCROLLING))
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
                        new Label(Text.literal("count: " + this.count)),
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
                new CoolSlider(2.0, value -> Text.literal("value: " + BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString())),
                new Padding(Insets.all(10)),
                new CoolSlider(null, value -> Text.literal("value: " + BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString()))
            );
        }
    }

    public static class CoolSlider extends StatefulWidget {

        public final @Nullable Double step;
        public final DoubleFunction<Text> textSupplier;

        public CoolSlider(@Nullable Double step, DoubleFunction<Text> textSupplier) {
            this.step = step;
            this.textSupplier = textSupplier;
        }

        @Override
        public WidgetState<CoolSlider> createState() {
            return new State();
        }

        public static class State extends WidgetState<CoolSlider> {

            private double value = 16;

            @Override
            public Widget build(BuildContext context) {
                return new Sized(
                    100.0,
                    20.0,
                    new MessageSlider(
                        this.value,
                        0,
                        32,
                        this.widget().step,
                        LayoutAxis.HORIZONTAL,
                        newValue -> setState(() -> this.value = newValue),
                        this.widget().textSupplier.apply(this.value)
                    )
                );
            }
        }
    }

    public static class TextInputTest extends StatefulWidget {
        @Override
        public WidgetState<TextInputTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<TextInputTest> {
            private final TextEditingController controller1 = new TextEditingController();
            private final TextEditingController controller2 = new TextEditingController();
            private final TextEditingController controller3 = new TextEditingController();

            @Override
            public Widget build(BuildContext context) {
                return new Column(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new Sized(
                        100.0,
                        50.0,
                        new TextBox(
                            this.controller1,
                            true,
                            true
                        )
                    ),
                    new Sized(
                        100.0,
                        50.0,
                        new TextBox(
                            this.controller2,
                            true,
                            false
                        )
                    ),
                    new Sized(
                        100.0,
                        20.0,
                        new TextBox(
                            this.controller3,
                            false,
                            false
                        )
                    )
                );
            }
        }
    }

    public static class BurningChyzButton extends StatelessWidget {
        public final Entity chyz;
        public final Runnable clickCallback;

        public BurningChyzButton(Entity chyz, Runnable clickCallback) {
            this.chyz = chyz;
            this.clickCallback = clickCallback;
        }

        @Override
        public Widget build(BuildContext context) {
            return new MouseArea(
                widget -> widget
                    .clickCallback((x, y) -> {
                        this.clickCallback.run();
                        UISounds.playButtonSound();
                    })
                    .cursorStyle(CursorStyle.HAND),
                new Stack(
                    new Center(
                        new Sized(
                            20.0,
                            20.0,
                            new Transform(
                                new Matrix4f().rotationZ((float) Math.toRadians(90)),
                                new EntityWidget(
                                    3.5,
                                    false,
                                    true,
                                    false,
                                    this.chyz
                                )
                            )
                        )
                    ),
                    new Transform(
                        new Matrix4f().translate(0, 0, 300),
                        new Label(
                            LabelStyle.SHADOW,
                            true,
                            Text.literal("burning chyz")
                        )
                    )
                )
            );
        }
    }

    public static class BurningChyzTest extends StatelessWidget {

        public final Entity chyz;
        public BurningChyzTest(Entity chyz) {
            this.chyz = chyz;
        }

        @Override
        public Widget build(BuildContext context) {
            return new Sized(
                250.0,
                250.0,
                new Panel(
                    OwoUIDrawContext.PANEL_NINE_PATCH_TEXTURE,
                    new Padding(
                        Insets.all(8),
                        new Panel(
                            OwoUIDrawContext.PANEL_INSET_NINE_PATCH_TEXTURE,
                            new EntityWidget(
                                1,
                                false,
                                true,
                                true,
                                this.chyz
                            )
                        )
                    )
                )
            );
        }
    }

    public static class ScrollTest extends StatefulWidget {

        @Override
        public WidgetState<?> createState() {
            return new State();
        }

        public static class State extends WidgetState<ScrollTest> {

            private final ScrollController horizontalController = new ScrollController();
            private final ScrollController verticalController = new ScrollController();

            @Override
            public Widget build(BuildContext context) {
                var text = BraidUtils.fold(
                    Wisdom.ALL_THE_WISDOM,
                    Text.empty(),
                    (result, wisdom) -> {
                        var wisdomColor = Color.ofHsv(
                            new java.util.Random(wisdom.hashCode()).nextFloat(), .75f, 1f
                        ).rgb();

                        return result.append(
                            Text.literal(wisdom + "\n").styled(style -> style.withColor(wisdomColor))
                        );
                    }
                );

                return new Sized(
                    210.0,
                    210.0,
                    new Column(
                        new Flexible(
                            new Row(
                                new Flexible(
                                    new Scrollable(
                                        true,
                                        true,
                                        this.horizontalController,
                                        this.verticalController,
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
                                            0,
                                            this.verticalController.maxOffset(),
                                            null,
                                            LayoutAxis.VERTICAL,
                                            this.verticalController::setOffset
                                        )
                                    )
                                )
                            )
                        ),
                        new Align(
                            Alignment.LEFT,
                            new Sized(
                                200.0,
                                10.0,
                                new ListenableBuilder(
                                    this.horizontalController,
                                    buildContext -> new Slider(
                                        this.horizontalController.offset(),
                                        0,
                                        this.horizontalController.maxOffset(),
                                        null,
                                        LayoutAxis.HORIZONTAL,
                                        this.horizontalController::setOffset
                                    )
                                )
                            )
                        )
                    )
                );
            }
        }
    }
}
