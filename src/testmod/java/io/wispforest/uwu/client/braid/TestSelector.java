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
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.basic.action.ActionTrigger;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.braid.widgets.button.Button;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.checkbox.BraidCheckbox;
import io.wispforest.owo.braid.widgets.checkbox.Checkbox;
import io.wispforest.owo.braid.widgets.checkbox.RawCheckbox;
import io.wispforest.owo.braid.widgets.button.RawButton;
import io.wispforest.owo.braid.widgets.cycle.MessageCyclingButton;
import io.wispforest.owo.braid.widgets.drag.DragArena;
import io.wispforest.owo.braid.widgets.drag.DragArenaElement;
import io.wispforest.owo.braid.widgets.flex.*;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.recipeviewer.RecipeViewerExclusionZone;
import io.wispforest.owo.braid.widgets.recipeviewer.RecipeViewerStack;
import io.wispforest.owo.braid.widgets.recipeviewer.StackDropArea;
import io.wispforest.owo.braid.widgets.scroll.ScrollController;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import io.wispforest.owo.braid.widgets.scroll.VerticallyScrollable;
import io.wispforest.owo.braid.widgets.sharedstate.ShareableState;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.braid.widgets.slider.*;
import io.wispforest.owo.braid.widgets.splitpane.MultiSplitPane;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import io.wispforest.owo.braid.widgets.textinput.TextBox;
import io.wispforest.owo.braid.widgets.textinput.TextEditingController;
import io.wispforest.owo.braid.widgets.vanilla.VanillaWidget;
import io.wispforest.owo.braid.widgets.window.Window;
import io.wispforest.owo.braid.widgets.window.WindowController;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.component.BraidComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.ViewerStack;
import io.wispforest.owo.util.Wisdom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;


import java.time.Duration;
import java.util.*;
import java.util.function.DoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestSelector extends StatefulWidget {

    public enum Tests {
        COUNTER, FLEX, DRAGGING, SPLIT_PANE, SLIDERS, TEXT_INPUT, BURNING_CHYZ, SCROLLING, INPUT, CYCLING, VANILLA, SHARED_STATE, STACKS, GRIDS, CONTRIBUTORS
    }

    @Override
    public WidgetState<TestSelector> createState() {
        return new State();
    }

    public static class State extends WidgetState<TestSelector> {

        private double xSkew = 0f;
        private double ySkew = 0f;

        private double rotat = 0f;

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
            System.out.println("reminder to decide how to handle mouse buttons in, buttons, sliders, text inputs, windows etc");

            var buttons = Arrays.stream(Tests.values()).map(test -> {
                if (test == Tests.BURNING_CHYZ) {
                    return new BurningChyzButton(this.chyz, () -> setState(() -> this.test = Tests.BURNING_CHYZ));
                } else {
                    return (Widget) new MessageButton(
                        Text.literal(test.name().toLowerCase(Locale.ROOT).replace('_', ' ')),
                        test != this.test ? () -> setState(() -> this.test = test) : null
                    );
                }
            }).collect(Collectors.toList());

            buttons.add(
                new MessageButton(
                    Text.literal("window"),
                    () -> BraidWindow.open(
                        "window moment??",
                        1200,
                        800,
                        new Box(
                            Color.ofRgb(0x1d2026),
                            new TestSelector()
                        )
                    )
                )
            );

            return new Stack(
                Alignment.CENTER,
                new Transform(
                    new Matrix4f().m01((float) Math.tan(this.xSkew)).m10((float) Math.tan(this.ySkew)).rotateZ((float) Math.toRadians(this.rotat)),
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
                            case GRIDS -> new GridsTest();
                            case CONTRIBUTORS -> new ContributorsTest();
                            case null -> new Center(new Label(Text.literal("select a test")));
                        }
                    )
                ),
                new Align(
                    Alignment.LEFT,
                    new HitTestTrap(
                        new Padding(
                            Insets.vertical(50).withLeft(5),
                            new Panel(
                                OwoUIDrawContext.PANEL_NINE_PATCH_TEXTURE,
                                new Padding(
                                    Insets.all(8),
                                    new VerticallyScrollable(
                                        new IntrinsicWidth(
                                            new Column(
                                                new Padding(Insets.all(2)),
                                                buttons
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                new Align(
                    Alignment.BOTTOM_RIGHT,
                    new Sized(
                        75, null,
                        new Column(
                            new Sized(
                                75, 20,
                                new MessageButton(
                                    Text.literal("reset"),
                                    () -> this.setState(() -> {
                                        this.xSkew = 0f;
                                        this.ySkew = 0f;
                                        this.rotat = 0f;
                                    })
                                )
                            ),
                            new Sized(
                                75, 20,
                                new MessageSlider(
                                    rotat,
                                    0d, 360d,
                                    null,
                                    LayoutAxis.HORIZONTAL,
                                    value -> this.setState(() -> this.rotat = value),
                                    Text.literal("rotat: " + formatDouble(this.rotat))
                                )
                            ),
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

            private boolean redundant = false;

            @Override
            public Widget build(BuildContext context) {
                return new Stack(
                    !this.redundant
                        ? new Grid(
                        LayoutAxis.VERTICAL,
                        3,
                        Grid.CellFit.tight(),
                        widget -> new Padding(Insets.all(5), widget),
                        null,
                        new Label(Text.literal("Discrete")),
                        new Label(Text.literal("Smooth")),
                        new Label(Text.literal("Basic")),
                        new CoolSlider(2.0, value -> Text.literal("v: " + formatDouble(value))),
                        new CoolSlider(null, value -> Text.literal("v: " + formatDouble(value))),
                        new Label(Text.literal("XY")),
                        new CoolXlyder(2.0, 2.0, (x, y) -> Text.literal("x: " + formatDouble(x) + "\ny: " + formatDouble(y))),
                        new CoolXlyder(null, null, (x, y) -> Text.literal("x: " + formatDouble(x) + "\ny: " + formatDouble(y))),
                        new Label(Text.literal("Range")),
                        new CoolRangeSlider(2.0, (min, max) -> Text.literal("v: " + formatDouble(min) + "-" + formatDouble(max))),
                        new CoolRangeSlider(null, (min, max) -> Text.literal("v: " + formatDouble(min) + "-" + formatDouble(max)))
                    )
                        : new IncrediblyRedundantSlider(),
                    new Align(
                        Alignment.BOTTOM,
                        new MessageButton(
                            Text.literal(this.redundant ? "no more redundancy" : "we love redundancy"),
                            () -> this.setState(() -> this.redundant = !this.redundant)
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
                return new Row(
                    MainAxisAlignment.CENTER,
                    CrossAxisAlignment.CENTER,
                    new Padding(Insets.horizontal(10)),
                    List.of(
                        new Column(
                            MainAxisAlignment.START,
                            CrossAxisAlignment.CENTER,
                            new Sized(
                                100.0,
                                50.0,
                                new TextBox(
                                    this.controller1,
                                    true,
                                    false,
                                    true,
                                    Style.EMPTY
                                )
                            ),
                            new Sized(
                                100.0,
                                50.0,
                                new TextBox(
                                    this.controller2,
                                    false,
                                    true,
                                    true,
                                    Style.EMPTY
                                )
                            ),
                            new Sized(
                                100.0,
                                20.0,
                                new TextBox(
                                    this.controller3,
                                    false,
                                    false,
                                    false,
                                    Style.EMPTY
                                )
                            )
                        ),
                        new ToggleFest()
                    )
                );
            }
        }

        public static class ToggleFest extends StatefulWidget {
            @Override
            public WidgetState<ToggleFest> createState() {
                return new State();
            }

            public static class State extends WidgetState<ToggleFest> {

                private final Entity chyz = EntityComponent.createRenderablePlayer(new GameProfile(
                    UUID.fromString("09de8a6d-86bf-4c15-bb93-ce3384ce4e96"),
                    "chyzman"
                ));

                private boolean checked = false;

                @Override
                public Widget build(BuildContext context) {
                    return new Column(
                        MainAxisAlignment.CENTER,
                        CrossAxisAlignment.START,
                        new Padding(Insets.vertical(5)),
                        List.of(
                            new LabelBox(
                                new RawCheckbox(
                                    this.checked,
                                    this::onUpdate,
                                    new Sized(
                                        20,
                                        20,
                                        new EntityWidget(1.5d, false, true, false, this.chyz)
                                    ),
                                    new Padding(Insets.none())
                                ),
                                "chyzbox"
                            ),
                            new LabelBox(
                                new RawCheckbox(
                                    this.checked,
                                    this::onUpdate,
                                    new SpriteWidget(Checkbox.TEXTURE, false),
                                    new SpriteWidget(new SpriteIdentifier(SpriteWidget.GUI_ATLAS_ID, Identifier.of("uwu", "czechbox")), false)
                                ),
                                this.checked ? "czechbox" : "checkbox"
                            ),
                            new LabelBox(
                                new Checkbox(this.checked, this::onUpdate),
                                "checkbox"
                            ),
                            new LabelBox(
                                new BraidCheckbox(this.checked, this::onUpdate),
                                "smolbox"
                            )
                        )
                    );
                }

                private void onUpdate(Boolean newState) {
                    this.setState(() -> {
                        this.checked = newState;
                        this.chyz.setOnFire(this.checked);
                    });
                }
            }

            public static class LabelBox extends StatelessWidget {
                public final Widget widget;
                public final String label;

                public LabelBox(Widget widget, String label) {
                    this.widget = widget;
                    this.label = label;
                }

                @Override
                public Widget build(BuildContext context) {
                    return new Row(
                        MainAxisAlignment.START,
                        CrossAxisAlignment.CENTER,
                        new Padding(Insets.horizontal(4)),
                        List.of(
                            new Sized(
                                20,
                                20,
                                new Center(
                                    this.widget
                                )
                            ),
                            new Label(Text.literal(this.label))
                        )
                    );
                }
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
            return new RawButton(
                this.clickCallback,
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
                            new RecipeViewerStack(
                                () -> ViewerStack.OfItem.of(Items.GOLD_BLOCK),
                                new StackDropArea(
                                    stack -> stack instanceof ViewerStack.OfItem,
                                    stack -> System.out.println("chyz: mmm i ate a " + ((ViewerStack.OfItem) stack).asStack()),
                                    new RecipeViewerExclusionZone(
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
                list.add(Text.literal("Help idk how to make this scroll to the bottom when i add shit (everyone laugh at this user)"));
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
                                            .clickCallback((x, y, button, modifiers) -> this.addToList(getMouseButtonName(button).append(" pressed at:\n").append(formatCoordinates(x, y))))
                                            .releaseCallback((x, y, button, modifiers) -> this.addToList(getMouseButtonName(button).append(" released at:\n").append(formatCoordinates(x, y))))
                                            .dragStartCallback((button, modifiers) -> this.addToList(getMouseButtonName(button).append(" drag started")))
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

            private boolean addToList(Text text) {
                this.setState(() -> {
                    this.inputs.add(text);
                    this.schedulePostLayoutCallback(() -> {
                        this.controller.setOffset(this.controller.maxOffset());
                    });
                });
                return false; // return false to allow other shit to happen:tm:
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

        private static final List<Integer> coolNumbers = List.of(
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        );

        @Override
        public WidgetState<CyclingTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<CyclingTest> {
            private CoolEnum selectedEnum = CoolEnum.FIRST;
            private CoolEnum selectedEnumNoWrap = CoolEnum.FIRST;
            private boolean selectedBoolean = false;
            private boolean selectedBooleanNoWrap = false;
            private String selectedString = coolStrings.get(0);
            private String selectedStringNoWrap = coolStrings.get(0);
            private int selectedInt = 0;
            private int selectedIntNoWrap = 0;

            @Override
            public Widget build(BuildContext context) {
                return new Grid(
                    LayoutAxis.VERTICAL,
                    4,
                    Grid.CellFit.tight(),
                    widget -> new Padding(Insets.all(5), widget),
                    null,
                    new Label(Text.literal("Cycler")),
                    new Label(Text.literal("No Wrap")),
                    new Label(Text.literal("Values")),
                    new Label(Text.literal("Enum")),
                    MessageCyclingButton.forEnum(
                        this.selectedEnum,
                        Text.literal(selectedEnum.name()),
                        (value, index) -> this.setState(() -> this.selectedEnum = value)
                    ),
                    MessageCyclingButton.forEnum(
                        this.selectedEnumNoWrap,
                        false,
                        Text.literal(selectedEnumNoWrap.name()),
                        (value, index) -> this.setState(() -> this.selectedEnumNoWrap = value)
                    ),
                    new Label(Text.literal(String.join(", ", Arrays.stream(CoolEnum.values()).map(Enum::name).collect(Collectors.toList())))),
                    new Label(Text.literal("Boolean")),
                    MessageCyclingButton.forBoolean(
                        this.selectedBoolean,
                        Text.literal(this.selectedBoolean ? "true" : "false"),
                        (value, index) -> this.setState(() -> this.selectedBoolean = value)
                    ),
                    null,
                    new Label(Text.literal("false, true")),
                    new Label(Text.literal("String")),
                    new MessageCyclingButton<>(
                        coolStrings,
                        coolStrings.indexOf(this.selectedString),
                        Text.literal(this.selectedString),
                        (value, index) -> this.setState(() -> this.selectedString = value)
                    ),
                    new MessageCyclingButton<>(
                        coolStrings,
                        coolStrings.indexOf(this.selectedStringNoWrap),
                        false,
                        Text.literal(this.selectedStringNoWrap),
                        (value, index) -> this.setState(() -> this.selectedStringNoWrap = value)
                    ),
                    new Label(Text.literal(String.join(", ", coolStrings))),
                    new Label(Text.literal("Int")),
                    new MessageCyclingButton<>(
                        coolNumbers,
                        this.selectedInt,
                        Text.literal(coolNumbers.get(this.selectedInt).toString()),
                        (value, index) -> this.setState(() -> this.selectedInt = index)
                    ),
                    new MessageCyclingButton<>(
                        coolNumbers,
                        this.selectedIntNoWrap,
                        false,
                        Text.literal(coolNumbers.get(this.selectedIntNoWrap).toString()),
                        (value, index) -> this.setState(() -> this.selectedIntNoWrap = index)
                    ),
                    new Label(
                        Text.literal(coolNumbers.stream()
                                         .map(String::valueOf)
                                         .collect(Collectors.joining(", ")))
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
                                widget.setPlaceholder(Text.literal("when the vanilla widget is no longer better than the braid widget"));
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
                                () -> SharedState.set(context, CounterState.class, state -> state.count += 1),
                                new Label(Text.literal("increment"))
                            ),
                            new Button(
                                () -> SharedState.set(context, CounterState.class, state -> state.dark = !state.dark),
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

    public static class GridsTest extends StatelessWidget {
        @Override
        public Widget build(BuildContext context) {
            var random = new java.util.Random(0);

            return new Center(
                new Grid(
                    LayoutAxis.VERTICAL,
                    2,
                    Grid.CellFit.loose(),
                    widget -> new Padding(Insets.all(10), widget),
                    new Sized(
                        90,
                        null,
                        new Grid(
                            LayoutAxis.VERTICAL,
                            3,
                            Grid.CellFit.loose(Alignment.BOTTOM_RIGHT),
                            new Sized(random.nextInt(15, 31), random.nextInt(15, 31), new Box(nextColor(random))),
                            new Sized(random.nextInt(15, 31), random.nextInt(15, 31), new Box(nextColor(random))),
                            new Sized(random.nextInt(15, 31), random.nextInt(15, 31), new Box(nextColor(random))),
                            new Sized(random.nextInt(15, 31), random.nextInt(15, 31), new Box(nextColor(random))),
                            new Sized(random.nextInt(15, 31), random.nextInt(15, 31), new Box(nextColor(random)))
                        )
                    ),
                    new IntrinsicWidth(
                        new Column(
                            MainAxisAlignment.CENTER,
                            CrossAxisAlignment.CENTER,
                            new Grid(
                                LayoutAxis.VERTICAL,
                                2,
                                Grid.CellFit.loose(),
                                new Sized(20, 40, new Box(Color.WHITE)),
                                new Sized(20, 20, new Box(Color.WHITE)),
                                new Sized(20, 20, new Box(Color.WHITE)),
                                new Sized(60, 40, new Box(Color.WHITE))
                            ),
                            new Button(() -> {}, new Label(Text.literal("a")))
                        )
                    ),
                    new IntrinsicWidth(
                        new Column(
                            MainAxisAlignment.CENTER,
                            CrossAxisAlignment.CENTER,
                            new Grid(
                                LayoutAxis.VERTICAL,
                                2,
                                Grid.CellFit.loose(),
                                new Sized(40, 40, new Box(Color.WHITE)),
                                new Sized(20, 20, new Box(Color.WHITE)),
                                new Sized(20, 20, new Box(Color.WHITE)),
                                new Sized(40, 40, new Box(Color.WHITE))
                            ),
                            new Button(() -> {}, new Label(Text.literal("a")))
                        )
                    )
                )
            );
        }

        private static Color nextColor(java.util.Random random) {
            return Color.ofHsv(random.nextFloat(), .75f, 1f);
        }
    }

    public static class ContributorsTest extends StatefulWidget {
        @Override
        public WidgetState<ContributorsTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<ContributorsTest> {

            private List<Contributor> contributors = this.genContributors();

            private List<Contributor> genContributors() {
                return List.of(
                    new Contributor(UUID.fromString("b6c2d403-bf7c-4e19-b7a2-f64c9e44e56a"), "glisco", Text.translatable("text.uwu.glisco")),
                    new Contributor(UUID.fromString("09de8a6d-86bf-4c15-bb93-ce3384ce4e96"), "chyzman", Text.translatable("text.uwu.chyz")),
                    new Contributor(UUID.fromString("517253c6-5ae6-4a70-8e8f-b8515321f774"), "Dragon_Seeker", TextOps.withColor("blodhgarm", 0xae0000)),
                    new Contributor(UUID.fromString("63db48b4-723a-4323-8d67-45679507fd82"), "GreatGrayOwl", TextOps.withColor("skibediah fœtus", 0x9b57d0)),
                    new Contributor(UUID.fromString("91a033f7-1dd3-4858-9c7b-8fb61ba6363d"), "Noaaan", Text.literal("no" + "a".repeat((int) (1 + Math.random() * 7)) + "n"))
                );
            }

            @Override
            public Widget build(BuildContext notContext) {
                return new SharedState<>(
                    MurderState::new,
                    new Builder(context -> {
                        var murders = SharedState.get(context, MurderState.class).murders;
                        return new Column(
                            MainAxisAlignment.CENTER,
                            CrossAxisAlignment.CENTER,
                            new Padding(Insets.all(10)),
                            new Label(murders.compareTo(BigInteger.ZERO) > 0 ? Text.literal("You have committed " + murders + " act" + (murders.compareTo(BigInteger.ONE) > 0 ? "s" : "") + " of " + Text.stringifiedTranslatable("uwu.homicide").getString() + " against the owo contributors!" + (murders.compareTo(BigInteger.valueOf(1000)) > 0 ? "... wtf bro" : "")).withColor(Colors.RED) : Text.literal("OWO Contributors")),
                            new Grid(
                                LayoutAxis.VERTICAL,
                                3,
                                Grid.CellFit.loose(),
                                Stream.concat(
                                        this.contributors.stream()
                                            .map(contributor -> {
                                                return new Padding(
                                                    Insets.all(8),
                                                    new Panel(
                                                        OwoUIDrawContext.PANEL_NINE_PATCH_TEXTURE,
                                                        new Padding(
                                                            Insets.all(8),
                                                            new Column(
                                                                MainAxisAlignment.CENTER,
                                                                CrossAxisAlignment.CENTER,
                                                                new Padding(Insets.top(4)),
                                                                List.of(
                                                                    new FirePlayer(new GameProfile(contributor.uuid, contributor.name)),
                                                                    new Label(LabelStyle.SHADOW, true, contributor.displayName),
                                                                    new RatingBar()
                                                                )
                                                            )
                                                        )
                                                    )
                                                );
                                            }),
                                        Stream.of(
                                            new Sized(
                                                20,
                                                20,
                                                new Button(
                                                    () -> setState(() -> this.contributors = this.genContributors()),
                                                    new Label(LabelStyle.SHADOW, true, Text.literal("☠"))
                                                )
                                            )
                                        )
                                    )
                                    .toList()
                            )
                        );
                    })
                );
            }

            public static class MurderState extends ShareableState {
                public BigInteger murders = BigInteger.ZERO;
            }

            public record Contributor(UUID uuid, String name, Text displayName) {}

            public static class FirePlayer extends StatefulWidget {

                public final GameProfile profile;

                public FirePlayer(GameProfile profile) {this.profile = profile;}

                @Override
                public WidgetState<FirePlayer> createState() {
                    return new FirePlayerState();
                }

                public static class FirePlayerState extends WidgetState<FirePlayer> {

                    private LivingEntity displayEntity;

                    private boolean dead = false;

                    @Override
                    public void init() {
                        this.displayEntity = EntityComponent.createRenderablePlayer(this.widget().profile);
                    }

                    @Override
                    public Widget build(BuildContext context) {
                        if (this.dead) this.displayEntity.setOnFire(false);
                        this.displayEntity.setHealth(dead ? 0 : 20);
                        this.displayEntity.deathTime = dead ? 20 : 0;
                        return Actions.click(
                            widget -> widget
                                .enterCallback(!this.dead ? () -> this.displayEntity.setOnFire(true) : null)
                                .exitCallback(!this.dead ? () -> this.displayEntity.setOnFire(false) : null)
                                .cursorStyle(!this.dead ? CursorStyle.CROSSHAIR : null),
                            this.dead ? null : () -> {
                                this.setState(() -> this.dead = true);
                                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_PLAYER_DEATH, 1));
                                SharedState.set(context, MurderState.class, state -> state.murders = state.murders.add(BigInteger.ONE));
                                scheduleDelayedCallback(Duration.ofSeconds(displayEntity.getUuid().equals(UUID.fromString("09de8a6d-86bf-4c15-bb93-ce3384ce4e96")) ? 1 : 3), () -> this.setState(() -> {
                                    this.dead = false;
                                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_TOTEM_USE, 1));
                                }));
                            },
                            new Panel(
                                Identifier.of("uwu", "contributors_panel"),
                                new Padding(
                                    Insets.bottom(8),
                                    new Sized(
                                        96,
                                        96,
                                        new EntityWidget(1.35, false, true, false, this.displayEntity)
                                    )
                                )
                            )
                        );
                    }
                }
            }

            public static class RatingBar extends StatefulWidget {
                @Override
                public WidgetState<RatingBar> createState() {
                    return new RatingBarState();
                }

                public static class RatingBarState extends WidgetState<RatingBar> {

                    private int selectedStarCount = 0;
                    private int hoverStarCount = 0;

                    @Override
                    public Widget build(BuildContext context) {
                        return new MouseArea(
                            widget -> widget
                                .exitCallback(() -> setState(() -> this.hoverStarCount = 0))
                                .cursorStyle(CursorStyle.HAND),
                            new Row(
                                this.star(0),
                                this.star(1),
                                this.star(2),
                                this.star(3),
                                this.star(4)
                            )
                        );
                    }

                    private Widget star(int idx) {
                        return new Actions(
                            widget -> widget
                                .addAction(ActionTrigger.CLICK, () -> setState(() -> this.selectedStarCount = idx + 1))
                                .addAction(ActionTrigger.SECONDARY_CLICK, () -> setState(() -> this.selectedStarCount = 0))
                                .enterCallback(() -> setState(() -> this.hoverStarCount = idx + 1)),
                            new Stack(
                                new SpriteWidget(
                                    new SpriteIdentifier(
                                        Identifier.of("textures/atlas/gui.png"),
                                        Identifier.of("uwu", (idx + 1) <= this.selectedStarCount ? "favorite_icon_selected" : "favorite_icon")
                                    ),
                                    false
                                ),
                                (idx + 1) <= this.hoverStarCount
                                    ? new SpriteWidget(
                                    new SpriteIdentifier(
                                        Identifier.of("textures/atlas/gui.png"),
                                        Identifier.of("uwu", "favorite_icon_hover")
                                    ),
                                    true
                                ) : new Padding(Insets.none())
                            )
                        );
                    }
                }
            }
        }
    }
}
