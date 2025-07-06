package io.wispforest.uwu.client.braid;

import com.mojang.authlib.GameProfile;
import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.EntityWidget;
import io.wispforest.owo.braid.widgets.ItemStackWidget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.button.Button;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.drag.DragArena;
import io.wispforest.owo.braid.widgets.drag.DragArenaElement;
import io.wispforest.owo.braid.widgets.flex.*;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.scroll.ScrollController;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import io.wispforest.owo.braid.widgets.scroll.VerticallyScrollable;
import io.wispforest.owo.braid.widgets.sharedstate.ShareableState;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.braid.widgets.slider.*;
import io.wispforest.owo.braid.widgets.splitpane.MultiSplitPane;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import io.wispforest.owo.braid.widgets.textinput.TextBox;
import io.wispforest.owo.braid.widgets.textinput.TextEditingController;
import io.wispforest.owo.braid.widgets.vanilla.VanillaWidget;
import io.wispforest.owo.braid.widgets.window.Window;
import io.wispforest.owo.braid.widgets.window.WindowController;
import io.wispforest.owo.ui.component.BraidComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.Wisdom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.DoubleFunction;

public class TestSelector extends StatefulWidget {

    public enum Tests {
        COUNTER, FLEX, DRAGGING, SPLIT_PANE, SLIDERS, TEXT_INPUT, BURNING_CHYZ, SCROLLING, INPUT, CYCLING, VANILLA, SHARED_STATE, STACKS
    }

    @Override
    public WidgetState<TestSelector> createState() {
        return new State();
    }

    public static class State extends WidgetState<TestSelector> {

        private Tests test = null;
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
            //TODO read that vvvv
            System.out.println("This is a reminder to decide how to handle mouse buttons in, buttons, sliders, text inputs, windows etc");
            return new Stack(
                Alignment.CENTER,
                new Center(
                    switch (this.test) {
                        case COUNTER -> new Counter();
                        case FLEX -> new FunnySwitchLayout();
                        case DRAGGING -> new DragArenaTest();
                        case SPLIT_PANE -> new SplitPaneTest();
                        case SLIDERS -> new SliderTest();
                        case TEXT_INPUT -> new TextInputTest();
                        case BURNING_CHYZ -> new BurningChyzTest(this.chyz);
                        case SCROLLING -> new ScrollTest();
                        case INPUT -> new InputTest();
                        case CYCLING -> new CyclingTest();
                        case VANILLA -> new VanillaTest();
                        case SHARED_STATE -> new SharedStateTest();
                        case STACKS -> new StacksTest();
                        case null -> new Center(new Label(Text.literal("select a test")));
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
                                            new Padding(Insets.all(2)),
                                            Arrays.stream(Tests.values()).map(test -> {
                                                if (test == Tests.BURNING_CHYZ) {
                                                    return new BurningChyzButton(this.chyz, () -> setState(() -> this.test = Tests.BURNING_CHYZ));
                                                } else {
                                                    return new MessageButton(
                                                        Text.literal(test.name().toLowerCase(Locale.ROOT).replace('_', ' ')),
                                                        test != this.test ? () -> setState(() -> this.test = test) : null
                                                    );
                                                }
                                            }).toList()
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
                                new MessageButton(
                                    Text.literal("+"),
                                    () -> this.setState(() -> this.count++)
                                )
                            ),
                            new Flexible(
                                new MessageButton(
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
                    new MessageButton(
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
                                    new MessageButton(Text.literal("window button :o"), () -> setState(() -> controller.expanded = !controller.expanded))
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
                        new MessageButton(
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
                    250.0,
                    200.0,
                    new MultiSplitPane(
                        LayoutAxis.HORIZONTAL,
                        MainAxisAlignment.START,
                        CrossAxisAlignment.CENTER,
                        List.of(
                            new Box(
                                Color.GREEN.interpolate(Color.ofArgb(0), .5f),
                                new Label(Text.literal("text here"))
                            ),
                            new Box(
                                Color.GREEN.interpolate(Color.ofArgb(0), .5f),
                                new Label(Text.literal("text here"))
                            ),
                            new Box(
                                Color.GREEN.interpolate(Color.ofArgb(0), .5f),
                                new Label(Text.literal("text here"))
                            ),
                            new Box(
                                Color.GREEN.interpolate(Color.ofArgb(0), .5f),
                                new Label(Text.literal("text here"))
                            )
                        )
                    )
                )
            );
        }
    }

    public static class SliderTest extends StatefulWidget {
        @Override
        public WidgetState<SliderTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<SliderTest> {

            private double xSkew = 0f;
            private double ySkew = 0f;

            private boolean redundant = false;

            @Override
            public Widget build(BuildContext context) {
                return new Stack(
                    new Transform(
                        new Matrix4f().m01((float) Math.tan(this.xSkew)).m10((float) Math.tan(this.ySkew)),
                        !this.redundant
                            ? new Column(
                            MainAxisAlignment.START,
                            CrossAxisAlignment.CENTER,
                            new Padding(Insets.all(10)),
                            List.of(
                                new Row(
                                    MainAxisAlignment.START,
                                    CrossAxisAlignment.CENTER,
                                    new Label(Text.literal("Discrete")),
                                    new Padding(Insets.all(10)),
                                    new Label(Text.literal("Smooth"))
                                ),
                                new Row(
                                    MainAxisAlignment.START,
                                    CrossAxisAlignment.CENTER,
                                    new Padding(Insets.all(10)),
                                    List.of(
                                        new Label(Text.literal("Basic")),
                                        new CoolSlider(2.0, value -> Text.literal("v: " + formatDouble(value))),
                                        new CoolSlider(null, value -> Text.literal("v: " + formatDouble(value)))
                                    )
                                ),
                                new Row(
                                    MainAxisAlignment.START,
                                    CrossAxisAlignment.CENTER,
                                    new Padding(Insets.all(10)),
                                    List.of(
                                        new Label(Text.literal("XY")),
                                        new CoolXlyder(2.0, 2.0, (x, y) -> Text.literal("x: " + formatDouble(x) + "\ny: " + formatDouble(y))),
                                        new CoolXlyder(null, null, (x, y) -> Text.literal("x: " + formatDouble(x) + "\ny: " + formatDouble(y)))
                                    )
                                ),
                                new Row(
                                    MainAxisAlignment.START,
                                    CrossAxisAlignment.CENTER,
                                    new Padding(Insets.all(10)),
                                    List.of(
                                        new Label(Text.literal("Range")),
                                        new CoolRangeSlider(2.0, (min, max) -> Text.literal("v: " + formatDouble(min) + "-" + formatDouble(max))),
                                        new CoolRangeSlider(null, (min, max) -> Text.literal("v: " + formatDouble(min) + "-" + formatDouble(max)))
                                    )
                                )
                            )
                        )
                            : new IncrediblyRedundantSlider()
                    ),
                    new Align(
                        Alignment.BOTTOM,
                        new MessageButton(
                            Text.literal(this.redundant ? "no more redundancy" : "we love redundancy"),
                            () -> this.setState(() -> this.redundant = !this.redundant)
                        )
                    ),
                    new Align(
                        Alignment.BOTTOM_RIGHT,
                        new Sized(
                            75.0,
                            75.0,
                            new MessageXlyder(
                                this.xSkew,
                                this.ySkew,
                                -.75, -.75,
                                .75, .75,
                                null, null,
                                (xValue, yValue) -> this.setState(() -> {
                                    this.xSkew = xValue;
                                    this.ySkew = yValue;
                                }),
                                Text.literal("x skew: " + (formatDouble(this.xSkew)) + "\ny skew: " + (formatDouble(this.ySkew)))
                            )
                        )
                    )
                );
            }
        }
    }

    public static String formatDouble(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString().replaceAll("(\\.0*|(?<=\\d)\\.0+)$", "");
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

    public static class CoolXlyder extends StatefulWidget {

        public final @Nullable Double xStep, yStep;
        public final MessageXlyder.XlyderMessageProvider textSupplier;

        public CoolXlyder(
            @Nullable Double xStep,
            @Nullable Double yStep,
            MessageXlyder.XlyderMessageProvider textSupplier
        ) {
            this.xStep = xStep;
            this.yStep = yStep;
            this.textSupplier = textSupplier;
        }

        @Override
        public WidgetState<CoolXlyder> createState() {
            return new State();
        }

        public static class State extends WidgetState<CoolXlyder> {

            private double x = 16;
            private double y = 16;

            @Override
            public Widget build(BuildContext context) {
                return new Sized(
                    100.0,
                    100.0,
                    new MessageXlyder(
                        this.x, this.y,
                        0, 0,
                        32, 32,
                        this.widget().xStep, this.widget().yStep,
                        (newX, newY) -> setState(() -> {
                            this.x = newX;
                            this.y = newY;
                        }),
                        this.widget().textSupplier.getMessage(this.x, this.y)
                    )
                );
            }
        }
    }

    public static class CoolRangeSlider extends StatefulWidget {

        public final @Nullable Double step;
        public final MessageRangeSlider.RangeSliderMessageProvider textSupplier;

        public CoolRangeSlider(@Nullable Double step, MessageRangeSlider.RangeSliderMessageProvider textSupplier) {
            this.step = step;
            this.textSupplier = textSupplier;
        }

        @Override
        public WidgetState<CoolRangeSlider> createState() {
            return new State();
        }

        public static class State extends WidgetState<CoolRangeSlider> {

            private double minValue = 10;
            private double maxValue = 20;

            @Override
            public Widget build(BuildContext context) {
                return new Sized(
                    100.0,
                    20.0,
                    new MessageRangeSlider(
                        this.minValue,
                        this.maxValue,
                        0,
                        32,
                        this.widget().step,
                        LayoutAxis.HORIZONTAL,
                        (min, max) -> setState(() -> {
                            this.minValue = min;
                            this.maxValue = max;
                        }),
                        this.widget().textSupplier.getMessage(this.minValue, this.maxValue)
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
                    .clickCallback((x, y, button) -> {
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
            private final WindowController controller = new WindowController(Size.square(200));

            @Override
            public void init() {
                super.init();
                this.controller.x = (MinecraftClient.getInstance().getWindow().getScaledWidth() - 200) / 2d;
                this.controller.y = (MinecraftClient.getInstance().getWindow().getScaledHeight() - 200) / 2d;
            }

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

                return new DragArena(
                    new Window(
                        false,
                        Text.literal("wisdom, but colored!"),
                        null,
                        this.controller,
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
                                                this.verticalController.maxOffset(),
                                                0,
                                                null,
                                                LayoutAxis.VERTICAL,
                                                this.verticalController::setOffset
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
                                                0,
                                                this.horizontalController.maxOffset(),
                                                null,
                                                LayoutAxis.HORIZONTAL,
                                                this.horizontalController::setOffset
                                            )
                                        )
                                    ),
                                    new Padding(Insets.all(5))
                                )
                            )
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
                        new Label(Text.literal("incredibly redundant slider™"))
                    ),
                    new Sized(
                        100.0,
                        15.0,
                        new Slider(
                            this.x,
                            0, 1,
                            null,
                            LayoutAxis.HORIZONTAL,
                            (x) -> this.setState(() -> this.x = x)
                        )
                    ),
                    new Row(
                        new Sized(
                            15.0,
                            100.0,
                            new Slider(
                                this.y,
                                0, 1,
                                null,
                                LayoutAxis.VERTICAL,
                                (y) -> this.setState(() -> this.y = y)
                            )
                        ),
                        new Sized(
                            100.0,
                            100.0,
                            new RawXlyder(
                                this.x, this.y,
                                0, 0, 1, 1,
                                null, null,
                                (x, y) -> this.setState(() -> {
                                    this.x = x;
                                    this.y = y;
                                }),
                                new Panel(ButtonComponent.DISABLED_TEXTURE),
                                new DefaultSliderHandle(),
                                Size.square((1 - this.y) * 16 + 8)
                            )
                        ),
                        new Sized(
                            15.0,
                            100.0,
                            new Slider(
                                this.y,
                                0, 1,
                                null,
                                LayoutAxis.VERTICAL,
                                (y) -> this.setState(() -> this.y = y)
                            )
                        )
                    ),
                    new Sized(
                        100.0,
                        15.0,
                        new RawSlider(
                            this.x,
                            0, 1,
                            null,
                            LayoutAxis.HORIZONTAL,
                            (x) -> this.setState(() -> this.x = x),
                            new Panel(ButtonComponent.DISABLED_TEXTURE),
                            new DefaultSliderHandle(),
                            24
                        )
                    ),
                    new Sized(
                        100.0,
                        15.0,
                        new RawSlider(
                            this.x,
                            0, 1,
                            null,
                            LayoutAxis.HORIZONTAL,
                            (x) -> this.setState(() -> this.x = x),
                            new Panel(ButtonComponent.DISABLED_TEXTURE),
                            new DefaultSliderHandle(),
                            18
                        )
                    ),
                    new Sized(
                        100.0,
                        15.0,
                        new RawSlider(
                            this.x,
                            0, 1,
                            null,
                            LayoutAxis.HORIZONTAL,
                            (x) -> this.setState(() -> this.x = x),
                            new Panel(ButtonComponent.DISABLED_TEXTURE),
                            new DefaultSliderHandle(),
                            12
                        )
                    ),
                    new Sized(
                        100.0,
                        15.0,
                        new RawSlider(
                            this.x,
                            0, 1,
                            null,
                            LayoutAxis.HORIZONTAL,
                            (x) -> this.setState(() -> this.x = x),
                            new Panel(ButtonComponent.DISABLED_TEXTURE),
                            new DefaultSliderHandle(),
                            6
                        )
                    )
                );
            }
        }
    }

    public static class InputTest extends StatefulWidget {
        @Override
        public WidgetState<InputTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<InputTest> {
            private final List<Text> inputs = Util.make(() -> {
                var list = new ArrayList<Text>();
                list.add(Text.literal("Help idk how to make this scroll to the bottom when i add shit"));
                return list;
            });
            private final ScrollController controller = new ScrollController();

            @Override
            public Widget build(BuildContext context) {
                return new Sized(
                    250.0,
                    null,
                    new Panel(
                        OwoUIDrawContext.PANEL_NINE_PATCH_TEXTURE,
                        new Padding(
                            Insets.all(8),
                            new Column(
                                new Label(Text.literal("Interact with V this V")),
                                new Sized(
                                    null, 200,
                                    new MouseArea(
                                        area -> area.cursorStyle(CursorStyle.HAND)
                                            .clickCallback((x, y, button) -> this.addToList(getMouseButtonName(button).append(" pressed at:\n").append(formatCoordinates(x, y))))
                                            .releaseCallback((x, y, button) -> this.addToList(getMouseButtonName(button).append(" released at:\n").append(formatCoordinates(x, y))))
                                            .dragStartCallback((button) -> this.addToList(getMouseButtonName(button).append(" drag started")))
                                            .dragEndCallback(() -> this.addToList(Text.literal("Drag ended")))
                                            .enterCallback(() -> this.addToList(Text.literal("Mouse entered")))
                                            .exitCallback(() -> this.addToList(Text.literal("Mouse exited"))),
                                        new KeyboardInput(
                                            input ->
                                                input.keyDownCallback((key, modifiers) -> this.addToList(getKeyName(key).append(" pressed")))
                                                    .keyUpCallback((key, modifiers) -> this.addToList(getKeyName(key).append(" released")))
                                                    .focusGainedCallback(() -> this.addToList(Text.literal("Focus gained")))
                                                    .focusLostCallback(() -> this.addToList(Text.literal("Focus lost")))
                                                    .charCallback((charCode, modifiers) -> this.addToList(Text.literal("Character typed: \"" + (char) charCode + "\""))),
                                            new Panel(
                                                OwoUIDrawContext.PANEL_INSET_NINE_PATCH_TEXTURE,
                                                new VerticallyScrollable(
                                                    controller,
                                                    new Column(
                                                        new Padding(Insets.vertical(2)),
                                                        this.inputs.stream().map(Label::new).toList()
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

            private void addToList(Text text) {
                this.setState(() -> {
                    this.inputs.add(text);
                    this.schedulePostLayoutCallback(() -> {
                        this.controller.setOffset(this.controller.maxOffset());
                    });
                });
            }

            private MutableText getKeyName(int key) {
                return Text.empty().append(InputUtil.Type.KEYSYM.createFromCode(key).getLocalizedText());
            }

            private MutableText getMouseButtonName(int button) {
                return Text.empty().append(InputUtil.Type.MOUSE.createFromCode(button).getLocalizedText());
            }

            private Text formatCoordinates(double x, double y) {
                return Text.literal("[x: " + formatDouble(x) + ", y: " + formatDouble(y) + "]");
            }
        }
    }

    public static class CyclingTest extends StatefulWidget {
        private static final List<String> coolStrings = List.of(
            "first", "second", "third", "fourth", "fifth"
        );

        @Override
        public WidgetState<CyclingTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<CyclingTest> {
            private CoolEnum selectedEnum = CoolEnum.FIRST;
            private boolean selectedBoolean = false;
            private String selectedString = "first";
            private int selectedInt = 0;

            @Override
            public Widget build(BuildContext context) {
                return new Column(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new Padding(Insets.all(10)),
                    List.of(
                        new Row(
                            MainAxisAlignment.START,
                            CrossAxisAlignment.CENTER,
                            new Padding(Insets.all(10)),
                            List.of(
                                new Label(Text.literal("Cycler")),
                                new Label(Text.literal("Values"))
                            )
//                        ),
//                        new Row(
//                            MainAxisAlignment.START,
//                            CrossAxisAlignment.CENTER,
//                            new Padding(Insets.all(10)),
//                            List.of(
//                                new Label(Text.literal("Enum")),
//                                new EnumCyclingButton<>(selectedEnum, value -> Text.literal("v: " + value), value -> this.setState(() -> this.selectedEnum = value)),
//                                new Column(
//                                    Arrays.stream(CoolEnum.values())
//                                        .map(coolEnum -> new Label(Text.literal(coolEnum.name())))
//                                        .toList()
//                                )
//                            )
//                        ),
//                        new Row(
//                            MainAxisAlignment.START,
//                            CrossAxisAlignment.CENTER,
//                            new Padding(Insets.all(10)),
//                            List.of(
//                                new Label(Text.literal("Boolean")),
//                                new CyclingButton<>(
//                                    selectedBoolean,
//                                    List.of(true, false),
//                                    value -> Text.literal("v: " + value),
//                                    value -> this.setState(() -> this.selectedBoolean = value)
//                                ),
//                                new Column(
//                                    List.of(
//                                        new Label(Text.literal("false")),
//                                        new Label(Text.literal("true"))
//                                    )
//                                )
//                            )
//                        ),
//                        new Row(
//                            MainAxisAlignment.START,
//                            CrossAxisAlignment.CENTER,
//                            new Padding(Insets.all(10)),
//                            List.of(
//                                new Label(Text.literal("String")),
//                                new CyclingButton<>(
//                                    selectedString,
//                                    coolStrings,
//                                    value -> this.setState(() -> this.selectedString = value),
//                                    Text.literal("v: " + selectedString)
//                                ),
//                                new Column(
//                                    coolStrings.stream()
//                                        .map(string -> new Label(Text.literal(string)))
//                                        .toList()
//                                )
//                            )
//                        ),
//                        new Row(
//                            MainAxisAlignment.START,
//                            CrossAxisAlignment.CENTER,
//                            new Padding(Insets.all(10)),
//                            List.of(
//                                new Label(Text.literal("Int")),
//                                new RawCyclingButton<>(
//                                    selectedInt,
//                                    amount -> this.selectedInt += amount,
//                                    value -> Text.literal("v: " + value),
//                                    value -> this.setState(() -> this.selectedInt = value)
//                                ),
//                                new Label(Text.literal("I'm not listing every number"))
//                            )
                        )
                    )
                );
            }
        }
    }

    public enum CoolEnum {
        FIRST,
        SECOND,
        THIRD,
        FOURTH,
        FIFTH
    }

    public static class VanillaTest extends StatefulWidget {
        @Override
        public WidgetState<VanillaTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<VanillaTest> {
            @Override
            public Widget build(BuildContext context) {
                return new Sized(
                    250.0,
                    250.0,
                    new Column(
                        new VanillaWidget<>(
                            Size.of(250, 20),
                            () ->
                                CheckboxWidget.builder(
                                    Text.literal("Checkbox"),
                                    MinecraftClient.getInstance().textRenderer
                                ).build()
                        ),
                        new VanillaWidget<>(
                            Size.of(250, 20),
                            () -> {
                                var widget = new TextFieldWidget(
                                    MinecraftClient.getInstance().textRenderer,
                                    0, 0, 100, 20,
                                    Text.literal("Text Field")
                                );
                                widget.setPlaceholder(Text.literal("when the vanilla widget is better than the braid widget"));
                                return widget;
                            }
                        ),
                        new VanillaWidget<>(
                            Size.of(250, 20),
                            () -> {
                                var adapter = OwoUIAdapter.createWithoutScreen(
                                    0, 0, 250, 20,
                                    Containers::verticalFlow
                                );
                                adapter.rootComponent.child(
                                    ButtonWidget.builder(
                                        Text.literal("A very very cool button"),
                                        button -> MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_GENERIC_EXPLODE, Random.create().nextFloat() * 2f))
                                    ).build()
                                ).child(
                                    new BraidComponent(
                                        new Column(
                                            new MessageButton(
                                                Text.literal("amogus"),
                                                () -> MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_ANVIL_BREAK, Random.create().nextFloat() * 2f))
                                            ),
                                            new MultiSplitPane(
                                                LayoutAxis.HORIZONTAL,
                                                MainAxisAlignment.START,
                                                CrossAxisAlignment.CENTER,
                                                List.of(
                                                    new Box(
                                                        Color.GREEN.interpolate(Color.ofArgb(0), .5f),
                                                        new Label(Text.literal("no way is"))
                                                    ),
                                                    new Box(
                                                        Color.GREEN.interpolate(Color.ofArgb(0), .5f),
                                                        new Label(Text.literal("that braid"))
                                                    ),
                                                    new Box(
                                                        Color.GREEN.interpolate(Color.ofArgb(0), .5f),
                                                        new Label(Text.literal("inside owoui"))
                                                    ),
                                                    new Box(
                                                        Color.GREEN.interpolate(Color.ofArgb(0), .5f),
                                                        new Label(Text.literal("inside braid?"))
                                                    )
                                                )
                                            )
                                        )
                                    ).sizing(Sizing.fixed(100))
                                ).allowOverflow(true);
                                adapter.inflateAndMount();
                                return adapter;
                            }
                        )
                    )
                );
            }
        }
    }

    public static class SharedStateTest extends StatefulWidget {
        @Override
        public WidgetState<SharedStateTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<SharedStateTest> {
            @Override
            public Widget build(BuildContext context) {
                return new Sized(
                    400,
                    250,
                    new Column(
                        new Flexible(new TheTest(false)),
                        new Flexible(new TheTest(true))
                    )
                );
            }

            public static class TheTest extends StatelessWidget {

                public final boolean nest;
                public TheTest(boolean nest) {
                    this.nest = nest;
                }

                @Override
                public Widget build(BuildContext context) {
                    return new SharedState<>(
                        CounterState::new,
                        new Row(
                            new Flexible(new LeftBody()),
                            new Flexible(new Center(new RightBody())),
                            this.nest ? new Flexible(2, new TheTest(false)) : new Padding(Insets.none())
                        )
                    );
                }
            }

            public static class LeftBody extends StatelessWidget {
                @Override
                public Widget build(BuildContext context) {
                    System.out.println("panel rebuild");
                    return new Panel(
                        SharedState.select(context, CounterState.class, state -> state.dark)
                            ? OwoUIDrawContext.DARK_PANEL_NINE_PATCH_TEXTURE
                            : OwoUIDrawContext.PANEL_NINE_PATCH_TEXTURE,
                        new CounterText()
                    );
                }
            }

            public static class RightBody extends StatelessWidget {
                @Override
                public Widget build(BuildContext context) {
                    return new IntrinsicWidth(
                        new Column(
                            new Button(
                                () -> {
                                    SharedState.set(context, CounterState.class, state -> state.count += 1);
                                    return true;
                                },
                                new Label(Text.literal("increment"))
                            ),
                            new Button(
                                () -> {
                                    SharedState.set(context, CounterState.class, state -> state.dark = !state.dark);
                                    return true;
                                },
                                new Label(Text.literal("toggle darkness"))
                            )
                        )
                    );
                }
            }

            public static class CounterText extends StatelessWidget {
                @Override
                public Widget build(BuildContext context) {
                    System.out.println("text rebuild");
                    return new Label(Text.literal("current state: " + SharedState.select(context, CounterState.class, state -> state.count)));
                }
            }
        }

        public static class CounterState extends ShareableState {
            public int count = 0;
            public boolean dark = false;
        }
    }

    public static class StacksTest extends StatelessWidget {
        @Override
        public Widget build(BuildContext context) {
            return new Center(
                new Row(
                    new Stack(
                        new Panel(OwoUIDrawContext.PANEL_NINE_PATCH_TEXTURE),
                        new StackBase(new Sized(100, 100, new Padding(Insets.none()))),
                        new Label(new LabelStyle(Alignment.BOTTOM_RIGHT, null, null, null), true, Text.literal("based corner text"))
                    ),
                    new Padding(Insets.horizontal(20)),
                    new Stack(
                        new Sized(100, 100, new Panel(OwoUIDrawContext.PANEL_NINE_PATCH_TEXTURE)),
                        new Label(new LabelStyle(Alignment.BOTTOM_RIGHT, null, null, null), true, Text.literal("failed corner text"))
                    ),
                    new Padding(Insets.horizontal(20)),
                    new IntrinsicWidth(
                        new IntrinsicHeight(
                            new Stack(
                                new Sized(100, 100, new Panel(OwoUIDrawContext.PANEL_NINE_PATCH_TEXTURE)),
                                new Label(new LabelStyle(Alignment.BOTTOM_RIGHT, null, null, null), true, Text.literal("intrinsic corner text"))
                            )
                        )
                    )
                )
            );
        }
    }
}
