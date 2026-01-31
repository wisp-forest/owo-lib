package io.wispforest.uwu.client.braid;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Axis;
import dev.kdl.KdlNode;
import dev.kdl.parse.Kdl2Parser;
import io.wispforest.endec.SerializationAttributes;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.format.gson.GsonSerializer;
import io.wispforest.owo.braid.animation.*;
import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.util.BraidToast;
import io.wispforest.owo.braid.util.kdl.BraidKdlEndecs;
import io.wispforest.owo.braid.util.kdl.KdlDeserializer;
import io.wispforest.owo.braid.util.kdl.KdlMapper;
import io.wispforest.owo.braid.util.kdl.WidgetEndec;
import io.wispforest.owo.braid.widgets.*;
import io.wispforest.owo.braid.widgets.animated.AnimatedAlign;
import io.wispforest.owo.braid.widgets.animated.AnimatedBox;
import io.wispforest.owo.braid.widgets.animated.AnimatedPadding;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.*;
import io.wispforest.owo.braid.widgets.checkbox.Checkbox;
import io.wispforest.owo.braid.widgets.checkbox.CheckboxStyle;
import io.wispforest.owo.braid.widgets.checkbox.DefaultCheckboxStyle;
import io.wispforest.owo.braid.widgets.combobox.ComboBox;
import io.wispforest.owo.braid.widgets.cycle.MessageCyclingButton;
import io.wispforest.owo.braid.widgets.drag.DragArena;
import io.wispforest.owo.braid.widgets.drag.DragArenaElement;
import io.wispforest.owo.braid.widgets.flex.*;
import io.wispforest.owo.braid.widgets.focus.FocusLevel;
import io.wispforest.owo.braid.widgets.focus.FocusPolicy;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.intents.*;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.object.BlockWidget;
import io.wispforest.owo.braid.widgets.object.EntityWidget;
import io.wispforest.owo.braid.widgets.object.ItemStackWidget;
import io.wispforest.owo.braid.widgets.overlay.Overlay;
import io.wispforest.owo.braid.widgets.overlay.OverlayEntryBuilder;
import io.wispforest.owo.braid.widgets.owoui.OwoUIWidget;
import io.wispforest.owo.braid.widgets.recipeviewer.RecipeViewerExclusionZone;
import io.wispforest.owo.braid.widgets.recipeviewer.RecipeViewerStack;
import io.wispforest.owo.braid.widgets.recipeviewer.StackDropArea;
import io.wispforest.owo.braid.widgets.scroll.*;
import io.wispforest.owo.braid.widgets.sharedstate.ShareableState;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.braid.widgets.slider.Incrementor;
import io.wispforest.owo.braid.widgets.slider.slider.MessageSlider;
import io.wispforest.owo.braid.widgets.slider.slider.Slider;
import io.wispforest.owo.braid.widgets.slider.xlyder.MessageXlyder;
import io.wispforest.owo.braid.widgets.splitpane.MultiSplitPane;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import io.wispforest.owo.braid.widgets.textinput.MaxLengthFormatter;
import io.wispforest.owo.braid.widgets.textinput.PatternFormatter;
import io.wispforest.owo.braid.widgets.textinput.TextBox;
import io.wispforest.owo.braid.widgets.textinput.TextEditingController;
import io.wispforest.owo.braid.widgets.vanilla.VanillaWidget;
import io.wispforest.owo.braid.widgets.window.Window;
import io.wispforest.owo.braid.widgets.window.WindowController;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.component.BraidComponent;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.Delta;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.ViewerStack;
import io.wispforest.owo.util.Wisdom;
import io.wispforest.uwu.client.Bikeshed;
import io.wispforest.uwu.client.HudTestWidget;
import io.wispforest.uwu.items.UwuItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;

import java.math.BigInteger;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.wispforest.uwu.client.braid.SliderTests.formatDouble;

public class TestSelector extends StatefulWidget {

    public enum Tests {
        COUNTER,
        FLEX,
        DRAGGING,
        SPLIT_PANE,
        SLIDERS,
        TEXT_INPUT,
        BURNING_CHYZ,
        SCROLLING,
        INPUT,
        CYCLING,
        VANILLA,
        SHARED_STATE,
        STACKS,
        GRIDS,
        CONTRIBUTORS,
        ANIMATIONS,
        NAVIGATOR,
        OVERLAY,
        TEXT,
        SPINNY_GHAST,
        OPTIMIZATION,
        AUTOMATIC_ANIMATION,
        KDL_WIDGETS
    }

    @Override
    public WidgetState<TestSelector> createState() {
        return new State();
    }

    public static class BurningChyz extends ShareableState {
        public final Player chyz;
        public BurningChyz(Player chyz) {this.chyz = chyz;}

        public static Player of(BuildContext context) {
            return SharedState.getWithoutDependency(context, BurningChyz.class).chyz;
        }
    }

    public static class State extends WidgetState<TestSelector> {

        private double xSkew = 0f;
        private double ySkew = 0f;

        private double rotat = 0f;
        private int fliptat = 0;
        private boolean bouncy = false;

        private Tests test = null;
        private Player chyz;

        @Override
        public void init() {
            this.chyz = EntityComponent.createRenderablePlayer(new GameProfile(
                UUID.fromString("09de8a6d-86bf-4c15-bb93-ce3384ce4e96"),
                "chyzman"
            ));

            this.chyz.setSharedFlagOnFire(true);
        }

        @Override
        public Widget build(BuildContext context) {
            var buttons = Arrays.stream(Tests.values()).map(test -> {
                if (test == Tests.BURNING_CHYZ) {
                    return new BurningChyzButton(this.chyz, () -> setState(() -> this.test = Tests.BURNING_CHYZ));
                } else {
                    return (Widget) new MessageButton(
                        Component.literal(test.name().toLowerCase(Locale.ROOT).replace('_', ' ')),
                        this.test != test ? () -> setState(() -> this.test = test) : null
                    );
                }
            }).collect(Collectors.toList());

            buttons.add(
                new MessageButton(
                    Component.literal("window"),
                    () -> BraidWindow.open(
                        "window moment??",
                        1200,
                        800,
                        new Box(
                            Color.rgb(0x1d2026),
                            new TestSelector()
                        )
                    )
                )
            );

            return new DefaultCheckboxStyle(
                new CheckboxStyle(null, null, SoundEvents.ENDER_DRAGON_FLAP),
                new Stack(
                    Alignment.CENTER,
                    new RotatedLayout(
                        this.fliptat,
                        new Stack(
                            Alignment.CENTER,
                            new Transform(
                                Util.make(() -> {
                                    var mat = new Matrix3x2f();
                                    mat.m01 = (float) Math.tan(this.xSkew);
                                    mat.m10 = (float) Math.tan(this.ySkew);
                                    mat.rotate((float) Math.toRadians(this.rotat));
                                    return mat;
                                }),
                                new SharedState<>(
                                    () -> new BurningChyz(this.chyz),
                                    new Center(
                                        switch (this.test) {
                                            case COUNTER -> new Counter();
                                            case FLEX -> new FunnySwitchLayout();
                                            case DRAGGING -> new DragArenaTest();
                                            case SPLIT_PANE -> new SplitPaneTest();
                                            case SLIDERS -> new SliderTests();
                                            case TEXT_INPUT -> new TextInputTest();
                                            case BURNING_CHYZ -> new BurningChyzTest();
                                            case SCROLLING -> new ScrollTest();
                                            case INPUT -> new InputTest();
                                            case CYCLING -> new CyclingTest();
                                            case VANILLA -> new VanillaTest();
                                            case SHARED_STATE -> new SharedStateTest();
                                            case STACKS -> new StacksTest();
                                            case GRIDS -> new GridsTest();
                                            case CONTRIBUTORS -> new ContributorsTest();
                                            case ANIMATIONS -> new AnimationsTest();
                                            case NAVIGATOR -> new NavigatorTest();
                                            case OVERLAY -> new OverlayTest();
                                            case TEXT -> new TextTest();
                                            case SPINNY_GHAST -> new SpinnyGhastTest();
                                            case OPTIMIZATION -> new OptimizationTest();
                                            case AUTOMATIC_ANIMATION -> new AutomaticAnimationTest();
                                            case KDL_WIDGETS -> new KdlWidgetsTest();
                                            case null -> new Center(new Label(Component.literal("select a test")));
                                        }
                                    )
                                )
                            ),
                            new Align(
                                Alignment.LEFT,
                                new Padding(
                                    Insets.vertical(50).withLeft(5),
                                    new HitTestTrap(
                                        new Panel(
                                            Panel.VANILLA_LIGHT,
                                            new Padding(
                                                Insets.all(8),
                                                new IntrinsicWidth(
                                                    new Column(
                                                        new Flexible(
                                                            new Panel(
                                                                Panel.VANILLA_INSET,
                                                                new Padding(
                                                                    Insets.all(2),
                                                                    new VerticallyScrollable(
                                                                        null,
                                                                        this.bouncy
                                                                            ? new ScrollAnimationSettings(Duration.ofMillis(750), Easing.OUT_BOUNCE)
                                                                            : new ScrollAnimationSettings(Duration.ofMillis(250), Easing.OUT_EXPO),

                                                                        new Column(
                                                                            new Padding(Insets.all(2)),
                                                                            buttons
                                                                        )
                                                                    )
                                                                )
                                                            )
                                                        ),
                                                        new Padding(Insets.vertical(3)),
                                                        new Row(
                                                            MainAxisAlignment.SPACE_AROUND,
                                                            CrossAxisAlignment.CENTER,
                                                            new Checkbox(CheckboxStyle.BRAID, this.bouncy, nowChecked -> this.setState(() -> this.bouncy = nowChecked)),
                                                            new Label(
                                                                LabelStyle.SHADOW,
                                                                true, Component.literal("bouncy?")
                                                            )
                                                        )
                                                    )
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
                        new Row(
                            MainAxisAlignment.START,
                            CrossAxisAlignment.END,
                            new Padding(
                                Insets.all(5),
                                new SurfaceDimensions()
                            ),
                            new Sized(
                                75, null,
                                new Column(
                                    new Sized(
                                        75, 20,
                                        new FocusPolicy(
                                            false,
                                            new Grid(
                                                LayoutAxis.VERTICAL,
                                                4,
                                                Grid.CellFit.tight(),
                                                new MessageButton(
                                                    Component.literal("↑"),
                                                    () -> Actions.invoke(Focusable.of(context).primaryFocus().context(), new Incrementor.IncrementIntent(LayoutAxis.VERTICAL, 1))
                                                ),
                                                new MessageButton(
                                                    Component.literal("↓"),
                                                    () -> Actions.invoke(Focusable.of(context).primaryFocus().context(), new Incrementor.IncrementIntent(LayoutAxis.VERTICAL, -1))
                                                ),
                                                new MessageButton(
                                                    Component.literal("←"),
                                                    () -> Actions.invoke(Focusable.of(context).primaryFocus().context(), new Incrementor.IncrementIntent(LayoutAxis.HORIZONTAL, -1))
                                                ),
                                                new MessageButton(
                                                    Component.literal("→"),
                                                    () -> Actions.invoke(Focusable.of(context).primaryFocus().context(), new Incrementor.IncrementIntent(LayoutAxis.HORIZONTAL, 1))
                                                )
                                            )
                                        )
                                    ),
                                    new Sized(
                                        75, 20,
                                        new ListenableBuilder(
                                            HudTestWidget.SHOW_TEST_HUD,
                                            listenableContext -> MessageCyclingButton.forBoolean(
                                                HudTestWidget.SHOW_TEST_HUD.value(),
                                                Component.literal("hud: " + (HudTestWidget.SHOW_TEST_HUD.value() ? "on" : "off")),
                                                (newValue, newIndex) -> HudTestWidget.SHOW_TEST_HUD.setValue(newValue)
                                            )
                                        )
                                    ),
                                    new Sized(
                                        75, 20,
                                        new MessageButton(
                                            Component.literal("yum"),
                                            () -> BraidToast.show(
                                                Duration.ofSeconds(5),
                                                null,
                                                new Row(
                                                    Stream.generate(() -> new Amogus(
                                                        new Box(Color.randomHue()),
                                                        new Box(Color.WHITE),
                                                        8
                                                    )).limit(100).toList()
                                                )
                                            )
                                        )
                                    ),
                                    new Sized(
                                        75, 20,
                                        new MessageButton(
                                            Component.literal("reset"),
                                            () -> this.setState(() -> {
                                                this.xSkew = 0f;
                                                this.ySkew = 0f;
                                                this.rotat = 0f;
                                                this.fliptat = 0;
                                            })
                                        )
                                    ),
                                    new Sized(
                                        75,
                                        null,
                                        new Column(
                                            new Padding(
                                                Insets.top(5),
                                                new Label(Component.literal("fliptat:"))
                                            ),
                                            new Row(
                                                MainAxisAlignment.START,
                                                CrossAxisAlignment.CENTER,
                                                new Flexible(
                                                    new MessageButton(Component.literal("-"), () -> this.setState(() -> this.fliptat -= 1))
                                                ),
                                                new Flexible(
                                                    new Label(Component.literal(String.valueOf(this.fliptat)))
                                                ),
                                                new Flexible(
                                                    new MessageButton(Component.literal("+"), () -> this.setState(() -> this.fliptat += 1))
                                                )
                                            )
                                        )
                                    ),
                                    new Sized(
                                        75, 20,
                                        new MessageSlider(
                                            rotat,
                                            Component.literal("rotat: " + formatDouble(this.rotat)), widget -> widget.range(0, 360).incrementStep(1),
                                            value -> this.setState(() -> this.rotat = value)
                                        )
                                    ),
                                    new Sized(
                                        75.0,
                                        75.0,
                                        new MessageXlyder(
                                            this.xSkew, this.ySkew,
                                            Component.literal("x skew: " + (formatDouble(this.xSkew)) + "\ny skew: " + (formatDouble(this.ySkew))), xlyder -> xlyder.range(-.75, .75),

                                            (xValue, yValue) -> this.setState(() -> {
                                                this.xSkew = xValue;
                                                this.ySkew = yValue;
                                            })
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

    public static class SurfaceDimensions extends StatefulWidget {
        @Override
        public WidgetState<SurfaceDimensions> createState() {
            return new State();
        }

        public static class State extends WidgetState<SurfaceDimensions> {

            private EventSource<Surface.ResizeCallback>.Subscription listener;

            private int width, height;

            @Override
            public void init() {
                var surface = AppState.of(this.context()).surface;
                this.width = surface.width();
                this.height = surface.height();

                this.listener = surface.onResize().subscribe((newWidth, newHeight) -> this.setState(() -> {
                    this.width = newWidth;
                    this.height = newHeight;
                }));
            }

            @Override
            public void dispose() {
                this.listener.cancel();
            }

            @Override
            public Widget build(BuildContext context) {
                return new Label(
                    LabelStyle.SHADOW,
                    true,
                    Component.literal(
                        "surface dimensions:\n" + this.width + ", " + this.height
                    ).withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal("this is hover text"))))
                );
            }
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
                        new Label(Component.literal("count: " + this.count)),
                        new Row(
                            new Flexible(
                                new MessageButton(
                                    Component.literal("+"),
                                    () -> this.setState(() -> this.count++)
                                )
                            ),
                            new Flexible(
                                new MessageButton(
                                    Component.literal("-"),
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
                        Component.literal("switch axis"),
                        () -> this.setState(() -> this.axis = this.axis.opposite())
                    ),
                    new Padding(Insets.all(5)),
                    new Panel(
                        Panel.VANILLA_LIGHT,
                        new Padding(
                            Insets.all(10),
                            new Column(
                                MainAxisAlignment.START,
                                CrossAxisAlignment.CENTER,
                                new Label(Component.literal("that's text")),
                                new Label(Component.literal("some more text")),
                                new Padding(
                                    Insets.top(5),
                                    new Counter()
                                )
                            )
                        )
                    ),
                    new Padding(Insets.all(5)),
                    new Panel(
                        Panel.VANILLA_DARK,
                        new Padding(
                            Insets.all(10),
                            new Column(
                                MainAxisAlignment.START,
                                CrossAxisAlignment.CENTER,
                                new Label(Component.literal("that's text")),
                                new Label(Component.literal("some more text")),
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
                    new FunnyDragText(Component.literal("me too!")),
                    new FunnyDragText(Component.literal("drag me!"))
                ));

                for (var controller : this.windows) {
                    elements.add(new Window(
                        true,
                        Component.literal("window " + controller.hashCode()),
                        () -> setState(() -> this.windows.remove(controller)),
                        controller,
                        Size.of(150, 75),
                        new Stack(
                            new Center(
                                new AspectRatio(
                                    16d / 9d,
                                    new Box(
                                        Color.WHITE.withA(.5),
                                        new Label(Component.literal("16:9 aspect ratio"))
                                    )
                                )
                            ),
                            new Align(
                                Alignment.TOP_LEFT,
                                new Column(
                                    new Label(Component.literal("a").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://chyz.xyz/box"))))),
                                    new MessageButton(Component.literal("window button :o"), () -> setState(() -> controller.toggleCollapsed()))
                                )
                            ),
                            new Align(
                                Alignment.BOTTOM_RIGHT,
                                new Tooltip(
                                    Component.literal("tooltip\nhere?"),
                                    new ItemStackWidget(BuiltInRegistries.ITEM.getRandom(RandomSource.create(controller.hashCode())).get().value().getDefaultInstance())
                                )
                            ),
                            new Align(
                                Alignment.BOTTOM_LEFT,
                                new BlockWidget(
                                    BuiltInRegistries.BLOCK.getRandom(RandomSource.create(controller.hashCode())).get().value().defaultBlockState()
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
                            Component.literal("add window"),
                            () -> setState(() -> this.windows.add(new WindowController()))
                        )
                    )
                );
            }
        }
    }

    public static class FunnyDragText extends StatefulWidget {

        public final Component text;

        public FunnyDragText(Component text) {
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
                            Panel.VANILLA_DARK,
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
                                Color.mix(.5, Color.GREEN, new Color(0)),
                                new Label(Component.literal("text here"))
                            ),
                            new Box(
                                Color.mix(.5, Color.GREEN, new Color(0)),
                                new Label(Component.literal("text here"))
                            ),
                            new Box(
                                Color.mix(.5, Color.GREEN, new Color(0)),
                                new Label(Component.literal("text here"))
                            ),
                            new Box(
                                Color.mix(.5, Color.GREEN, new Color(0)),
                                new Label(Component.literal("text here"))
                            )
                        )
                    )
                )
            );
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
            private final TextEditingController controller4 = new TextEditingController();
            private final TextEditingController controller5 = new TextEditingController();

            private Color numbersColor = Color.randomHue();

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
                                    widget -> widget
                                        .suggestion(Component.literal("Soft Wrapping Moment"))
                                        .formatter(PatternFormatter.deny(Pattern.compile("\\*\\*\\*\\*\\*"), "Penis"))
                                        .formatter(PatternFormatter.deny(Pattern.compile("\\*\\*\\*\\*"), "cunt"))
                                )
                            ),
                            new Sized(
                                100.0,
                                50.0,
                                new TextBox(
                                    this.controller2,
                                    widget -> widget
                                        .softWrap(false)
                                        .autoFocus(true)
                                        .placeholder(Component.literal("No Soft Wrapping Moment (also auto focused)"))
                                )
                            ),
                            new Sized(
                                100.0,
                                20,
                                new Focusable(
                                    widget -> widget
                                        .skipTraversal(true)
                                        .keyDownCallback((keyCode, modifiers) -> {
                                            if (keyCode != GLFW.GLFW_KEY_ENTER || !modifiers.equals(KeyModifiers.NONE)) {
                                                return false;
                                            }

                                            this.setState(() -> this.numbersColor = Color.randomHue());
                                            return true;
                                        }),
                                    new TextBox(
                                        this.controller3,
                                        widget -> widget
                                            .baseStyle(Style.EMPTY.withColor(this.numbersColor.argb()))
                                            .formatter(PatternFormatter.allow(Pattern.compile("[0-9]")))
                                            .placeholder(Component.literal("only numbers"))
                                    )
                                )
                            ),
                            new Sized(
                                100.0,
                                20.0,
                                new TextBox(
                                    this.controller4,
                                    widget -> widget
                                        .singleLine()
                                        .placeholder(Component.literal("Single Line Moment"))
                                )
                            ),
                            new Sized(
                                100.0,
                                20.0,
                                new TextBox(
                                    this.controller5,
                                    widget -> widget
                                        .singleLine()
                                        .formatter(new MaxLengthFormatter(3))
                                        .placeholder(Component.literal("3 chars, TILI"))
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
                                new Checkbox(
                                    new CheckboxStyle(
                                        active -> new Sized(
                                            20,
                                            20,
                                            new EntityWidget(1.5d, this.chyz, widget -> widget.displayMode(EntityWidget.DisplayMode.CURSOR))
                                        ),
                                        EmptyWidget.INSTANCE,
                                        null
                                    ), this.checked,
                                    this::onUpdate
                                ),
                                "chyzbox"
                            ),
                            new LabelBox(
                                new Checkbox(
                                    new CheckboxStyle(
                                        null,
                                        new Center(new SpriteWidget(new Material(SpriteWidget.GUI_ATLAS_ID, Identifier.fromNamespaceAndPath("uwu", "czechbox")))),
                                        null
                                    ), this.checked,
                                    this::onUpdate
                                ),
                                this.checked ? "czechbox" : "checkbox"
                            ),
                            new LabelBox(
                                new Checkbox(this.checked, this::onUpdate),
                                "checkbox"
                            ),
                            new LabelBox(
                                new Checkbox(CheckboxStyle.BRAID, this.checked, this::onUpdate),
                                "smolbox"
                            )
                        )
                    );
                }

                private void onUpdate(Boolean newState) {
                    this.setState(() -> {
                        this.checked = newState;
                        this.chyz.setSharedFlagOnFire(this.checked);
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
                            new Label(Component.literal(this.label))
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
            return new Clickable(
                Clickable.alwaysClick(this.clickCallback),
                new Stack(
                    new Center(
                        new Sized(
                            20.0,
                            20.0,
                            new Transform(
                                new Matrix3x2f().rotation((float) Math.toRadians(90)),
                                new EntityWidget(
                                    3.5,
                                    this.chyz,
                                    widget -> widget.displayMode(EntityWidget.DisplayMode.CURSOR)
                                )
                            )
                        )
                    ),
                    new Label(
                        LabelStyle.SHADOW,
                        true,
                        Component.literal("burning chyz")
                    )
                )
            );
        }
    }

    public static class BurningChyzTest extends StatelessWidget {

        @Override
        public Widget build(BuildContext context) {
            return new Row(
                MainAxisAlignment.START,
                CrossAxisAlignment.CENTER,
                new Padding(Insets.horizontal(10)),
                List.of(
                    new Sized(
                        250.0,
                        250.0,
                        new Panel(
                            Panel.VANILLA_LIGHT,
                            new Padding(
                                Insets.all(8),
                                new Panel(
                                    Panel.VANILLA_INSET,
                                    new RecipeViewerStack(
                                        () -> ViewerStack.OfItem.of(Items.GOLD_BLOCK),
                                        new StackDropArea(
                                            stack -> stack instanceof ViewerStack.OfItem,
                                            stack -> System.out.println("chyz: mmm i ate a " + ((ViewerStack.OfItem) stack).asStack()),
                                            new RecipeViewerExclusionZone(
                                                new EntityWidget(
                                                    1,
                                                    BurningChyz.of(context),
                                                    widget -> widget.displayMode(EntityWidget.DisplayMode.CURSOR)
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    new Amogus(
                        new Box(Color.RED),
                        new Box(Color.WHITE),
                        16
                    ),
                    new Bikeshed()
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

    public static class InputTest extends StatefulWidget {
        @Override
        public WidgetState<InputTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<InputTest> {
            private final List<Component> inputs = Util.make(() -> {
                var list = new ArrayList<Component>();
                list.add(Component.literal("Help idk how to make this scroll to the bottom when i add shit (everyone laugh at this user)"));
                return list;
            });
            private final ScrollController controller = new ScrollController(this);

            @Override
            public Widget build(BuildContext context) {
                return new Sized(
                    250.0,
                    null,
                    new Panel(
                        Panel.VANILLA_LIGHT,
                        new Padding(
                            Insets.all(8),
                            new Column(
                                new Label(Component.literal("Interact with V this V")),
                                new Sized(
                                    null, 200,
                                    new MouseArea(
                                        area -> area.cursorStyle(CursorStyle.HAND)
                                            .clickCallback((x, y, button, modifiers) -> this.addToList(getMouseButtonName(button).append(" pressed at:\n").append(formatCoordinates(x, y))))
                                            .releaseCallback((x, y, button, modifiers) -> this.addToList(getMouseButtonName(button).append(" released at:\n").append(formatCoordinates(x, y))))
                                            .dragStartCallback((button, modifiers) -> this.addToList(getMouseButtonName(button).append(" drag started")))
                                            .dragEndCallback(() -> this.addToList(Component.literal("Drag ended")))
                                            .enterCallback(() -> this.addToList(Component.literal("Mouse entered")))
                                            .exitCallback(() -> this.addToList(Component.literal("Mouse exited"))),
                                        new Focusable(
                                            input ->
                                                input.keyDownCallback((key, modifiers) -> this.addToList(getKeyName(key).append(" pressed")))
                                                    .keyUpCallback((key, modifiers) -> this.addToList(getKeyName(key).append(" released")))
                                                    .focusGainedCallback(() -> this.addToList(Component.literal("Focus gained")))
                                                    .focusLostCallback(() -> this.addToList(Component.literal("Focus lost")))
                                                    .charCallback((charCode, modifiers) -> this.addToList(Component.literal("Character typed: \"" + (char) charCode + "\""))),
                                            new Panel(
                                                Panel.VANILLA_INSET,
                                                new VerticallyScrollable(
                                                    controller,
                                                    null,
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

            private boolean addToList(Component text) {
                this.setState(() -> {
                    this.inputs.add(text);
                    this.schedulePostLayoutCallback(() -> {
                        this.controller.jumpTo(this.controller.maxOffset());
                    });
                });
                return false; // return false to allow other shit to happen:tm:
            }

            private MutableComponent getKeyName(int key) {
                return Component.empty().append(InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName());
            }

            private MutableComponent getMouseButtonName(int button) {
                return Component.empty().append(InputConstants.Type.MOUSE.getOrCreate(button).getDisplayName());
            }

            private Component formatCoordinates(double x, double y) {
                return Component.literal("[x: " + formatDouble(x) + ", y: " + formatDouble(y) + "]");
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

            private boolean altButtons = true;

            @Override
            public Widget build(BuildContext context) {
                return new Column(
                    MainAxisAlignment.CENTER,
                    CrossAxisAlignment.CENTER,
                    new DefaultButtonStyle(
                        this.altButtons ? new ButtonStyle(
                            (active, child) -> new HoverableBuilder(
                                (hoverableContext, hovered, hoverableChild) -> {
                                    return new Box(
                                        active
                                            ? (hovered || Focusable.levelOf(hoverableContext) == FocusLevel.HIGHLIGHT ? Color.BLUE : Color.WHITE)
                                            : Color.BLACK,
                                        true,
                                        hoverableChild
                                    );
                                },
                                child
                            ),
                            Insets.all(10.0),
                            SoundEvents.GENERIC_EXPLODE.value()
                        ) : ButtonStyle.DEFAULT,
                        new Grid(
                            LayoutAxis.VERTICAL,
                            4,
                            Grid.CellFit.tight(),
                            widget -> new Padding(Insets.all(5), widget),
                            null,
                            new Label(Component.literal("Cycler")),
                            new Label(Component.literal("No Wrap")),
                            new Label(Component.literal("Values")),
                            new Label(Component.literal("Enum")),
                            MessageCyclingButton.forEnum(
                                this.selectedEnum,
                                Component.literal(selectedEnum.name()),
                                (value, index) -> this.setState(() -> this.selectedEnum = value)
                            ),
                            MessageCyclingButton.forEnum(
                                this.selectedEnumNoWrap,
                                false,
                                Component.literal(selectedEnumNoWrap.name()),
                                (value, index) -> this.setState(() -> this.selectedEnumNoWrap = value)
                            ),
                            new Label(Component.literal(String.join(", ", Arrays.stream(CoolEnum.values()).map(Enum::name).collect(Collectors.toList())))),
                            new Label(Component.literal("Boolean")),
                            MessageCyclingButton.forBoolean(
                                this.selectedBoolean,
                                Component.literal(this.selectedBoolean ? "true" : "false"),
                                (value, index) -> this.setState(() -> this.selectedBoolean = value)
                            ),
                            new MessageCyclingButton<>(
                                List.of(false, true), this.selectedBooleanNoWrap, false,
                                Component.literal(this.selectedBooleanNoWrap ? "true" : "false"),
                                (value, index) -> this.setState(() -> this.selectedBooleanNoWrap = value)
                            ),
                            new Label(Component.literal("false, true")),
                            new Label(Component.literal("String")),
                            new MessageCyclingButton<>(
                                coolStrings,
                                this.selectedString,
                                Component.literal(this.selectedString),
                                (value, index) -> this.setState(() -> this.selectedString = value)
                            ),
                            new MessageCyclingButton<>(
                                coolStrings,
                                this.selectedStringNoWrap,
                                false,
                                Component.literal(this.selectedStringNoWrap),
                                (value, index) -> this.setState(() -> this.selectedStringNoWrap = value)
                            ),
                            new Label(Component.literal(String.join(", ", coolStrings))),
                            new Label(Component.literal("Int")),
                            new MessageCyclingButton<>(
                                coolNumbers,
                                this.selectedInt,
                                Component.literal(coolNumbers.get(this.selectedInt).toString()),
                                (value, index) -> this.setState(() -> this.selectedInt = index)
                            ),
                            new MessageCyclingButton<>(
                                coolNumbers,
                                this.selectedIntNoWrap,
                                false,
                                Component.literal(coolNumbers.get(this.selectedIntNoWrap).toString()),
                                (value, index) -> this.setState(() -> this.selectedIntNoWrap = index)
                            ),
                            new Label(
                                Component.literal(coolNumbers.stream()
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(", ")))
                            )
                        )
                    ),
                    new Row(
                        MainAxisAlignment.CENTER,
                        CrossAxisAlignment.CENTER,
                        new Checkbox(CheckboxStyle.BRAID, this.altButtons, nowChecked -> this.setState(() -> this.altButtons = nowChecked)),
                        new Padding(
                            Insets.left(5),
                            Label.literal("alternate buttons")
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
                                net.minecraft.client.gui.components.Checkbox.builder(
                                    Component.literal("Checkbox"),
                                    Minecraft.getInstance().font
                                ).build()
                        ),
                        new VanillaWidget<>(
                            Size.of(250, 20),
                            () -> {
                                var widget = new EditBox(
                                    Minecraft.getInstance().font,
                                    0, 0, 100, 20,
                                    Component.literal("Text Field")
                                );
                                widget.setHint(Component.literal("when the vanilla widget is no longer better than the braid widget"));
                                return widget;
                            }
                        ),
                        new OwoUIWidget(
                            () -> {
                                var root = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
                                root.child(
                                    UIComponents.button(
                                        Component.literal("A very very cool button"),
                                        button -> Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE, RandomSource.create().nextFloat() * 2f))
                                    )
                                ).child(
                                    new BraidComponent(
                                        new Column(
                                            new MessageButton(
                                                Component.literal("amogus"),
                                                () -> Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_BREAK, RandomSource.create().nextFloat() * 2f))
                                            ),
                                            // idk why this needs to be here but if it's not the split pane becomes
                                            // infinity sized
                                            new Sized(
                                                180, 180,
                                                new MultiSplitPane(
                                                    LayoutAxis.HORIZONTAL,
                                                    MainAxisAlignment.START,
                                                    CrossAxisAlignment.CENTER,
                                                    List.of(
                                                        new Box(
                                                            Color.mix(.5, Color.GREEN, new Color(0)),
                                                            new Label(Component.literal("no way is"))
                                                        ),
                                                        new Box(
                                                            Color.mix(.5, Color.GREEN, new Color(0)),
                                                            new Label(Component.literal("that braid"))
                                                        ),
                                                        new Box(
                                                            Color.mix(.5, Color.GREEN, new Color(0)),
                                                            new Label(Component.literal("inside owoui"))
                                                        ),
                                                        new Box(
                                                            Color.mix(.5, Color.GREEN, new Color(0)),
                                                            new Label(Component.literal("inside braid?"))
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    ).sizing(Sizing.fixed(200))
                                ).allowOverflow(true);
                                return root;
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
                            ? Panel.VANILLA_DARK
                            : Panel.VANILLA_LIGHT,
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
                                new Label(Component.literal("increment"))
                            ),
                            new Button(
                                () -> SharedState.set(context, CounterState.class, state -> state.dark = !state.dark),
                                new Label(Component.literal("toggle darkness"))
                            )
                        )
                    );
                }
            }

            public static class CounterText extends StatelessWidget {
                @Override
                public Widget build(BuildContext context) {
                    System.out.println("text rebuild");
                    return new Label(Component.literal("current state: " + SharedState.select(context, CounterState.class, state -> state.count)));
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
                        new Panel(Panel.VANILLA_LIGHT),
                        new StackBase(new Sized(100, 100, new Padding(Insets.none()))),
                        new Label(new LabelStyle(Alignment.BOTTOM_RIGHT, null, null, null), true, Component.literal("based corner text"))
                    ),
                    new Padding(Insets.horizontal(20)),
                    new Stack(
                        new Sized(100, 100, new Panel(Panel.VANILLA_LIGHT)),
                        new Label(new LabelStyle(Alignment.BOTTOM_RIGHT, null, null, null), true, Component.literal("failed corner text"))
                    ),
                    new Padding(Insets.horizontal(20)),
                    new IntrinsicWidth(
                        new IntrinsicHeight(
                            new Stack(
                                new Sized(100, 100, new Panel(Panel.VANILLA_LIGHT)),
                                new Label(new LabelStyle(Alignment.BOTTOM_RIGHT, null, null, null), true, Component.literal("intrinsic corner text"))
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
                            new Button(() -> {}, new Label(Component.literal("a")))
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
                            new Button(() -> {}, new Label(Component.literal("a")))
                        )
                    )
                )
            );
        }

        private static Color nextColor(java.util.Random random) {
            return Color.hsv(random.nextFloat(), .75f, 1f);
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
                    new Contributor(UUID.fromString("b6c2d403-bf7c-4e19-b7a2-f64c9e44e56a"), "glisco", Component.translatable("text.uwu.glisco")),
                    new Contributor(UUID.fromString("09de8a6d-86bf-4c15-bb93-ce3384ce4e96"), "chyzman", Component.translatable("text.uwu.chyz")),
                    new Contributor(UUID.fromString("517253c6-5ae6-4a70-8e8f-b8515321f774"), "Dragon_Seeker", TextOps.withColor("blodhgarm", 0xae0000)),
                    new Contributor(UUID.fromString("63db48b4-723a-4323-8d67-45679507fd82"), "GreatGrayOwl", TextOps.withColor("skibediah fœtus", 0x9b57d0)),
                    new Contributor(UUID.fromString("91a033f7-1dd3-4858-9c7b-8fb61ba6363d"), "Noaaan", Component.literal("no" + "a".repeat((int) (1 + Math.random() * 7)) + "n"))
                );
            }

            @Override
            public Widget build(BuildContext notContext) {
                return new SharedState<>(
                    MurderState::new,
                    new Builder(context -> {
                        var murders = SharedState.get(context, MurderState.class).murders;
                        var eepies = SharedState.get(context, MurderState.class).eepies;
                        var bed = SharedState.get(context, MurderState.class).bed;
                        return new Column(
                            MainAxisAlignment.CENTER,
                            CrossAxisAlignment.CENTER,
                            new Padding(Insets.all(10)),
                            new Label(murders.compareTo(BigInteger.ZERO) > 0 ? Component.literal("You have committed " + murders + " act" + (murders.compareTo(BigInteger.ONE) > 0 ? "s" : "") + " of " + Component.translatableEscape("uwu.homicide").getString() + " against the owo contributors!" + (murders.compareTo(BigInteger.valueOf(1000)) > 0 ? "... wtf bro" : "")).withColor(CommonColors.RED) : Component.literal("OWO Contributors")),
                            new Label(eepies.compareTo(BigInteger.ZERO) > 0 ? Component.literal("You have committed " + eepies + " act" + (eepies.compareTo(BigInteger.ONE) > 0 ? "s" : "") + " of " + Component.translatableEscape("uwu.eepy").getString() + " against the owo contributors!" + (murders.compareTo(BigInteger.valueOf(1000)) > 0 ? "... idk" : "")).withColor(((BlockItem) bed.getItem()).getBlock().defaultBlockState().getMapColor(Minecraft.getInstance().level, BlockPos.ZERO).col) : Component.empty()),
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
                                                        Panel.VANILLA_LIGHT,
                                                        new Padding(
                                                            Insets.all(8),
                                                            new Column(
                                                                MainAxisAlignment.CENTER,
                                                                CrossAxisAlignment.CENTER,
                                                                new Padding(Insets.top(4)),
                                                                List.of(
                                                                    new FirePlayer(new GameProfile(contributor.uuid, contributor.name)),
                                                                    new Label(
                                                                        LabelStyle.SHADOW,
                                                                        true,
                                                                        contributor.displayName().copy().setStyle(contributor.displayName.copy().getStyle().withHoverEvent(new HoverEvent.ShowEntity(new HoverEvent.EntityTooltipInfo(EntityType.PLAYER, contributor.uuid, contributor.displayName))))
                                                                    ),
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
                                                    new Label(LabelStyle.SHADOW, true, Component.literal("☠"))
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
                public BigInteger eepies = BigInteger.ZERO;
                private ItemStack bed = UwuItems.BRAID.getDefaultInstance();
            }

            public record Contributor(UUID uuid, String name, Component displayName) {}

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
                        if (this.dead) this.displayEntity.setSharedFlagOnFire(false);
                        this.displayEntity.setHealth(dead ? 0 : 20);
                        this.displayEntity.deathTime = dead ? 20 : 0;
                        return Interactable.primary(
                            this.dead ? null : () -> {
                                this.setState(() -> {
                                    this.dead = true;
                                });
                                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_DEATH, 1));
                                SharedState.set(context, MurderState.class, state -> {
                                    if (!displayEntity.getUUID().equals(UUID.fromString("91a033f7-1dd3-4858-9c7b-8fb61ba6363d"))) {
                                        state.murders = state.murders.add(BigInteger.ONE);
                                    } else {
                                        state.eepies = state.eepies.add(BigInteger.ONE);
                                        state.bed = BuiltInRegistries.ITEM.getRandomElementOf(ItemTags.BEDS, RandomSource.create()).get().value().getDefaultInstance();
                                    }
                                });
                                scheduleDelayedCallback(
                                    Duration.ofSeconds(displayEntity.getUUID().equals(UUID.fromString("09de8a6d-86bf-4c15-bb93-ce3384ce4e96")) ? 1 : 3), () -> this.setState(() -> {
                                        this.dead = false;
                                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.TOTEM_USE, 1));
                                    })
                                );
                            },
                            widget -> widget
                                .enterCallback(!this.dead ? () -> this.displayEntity.setSharedFlagOnFire(true) : null)
                                .exitCallback(!this.dead ? () -> this.displayEntity.setSharedFlagOnFire(false) : null)
                                .cursorStyle(!this.dead ? CursorStyle.CROSSHAIR : null),
                            new Stack(
                                new StackBase(
                                    new Panel(
                                        Identifier.fromNamespaceAndPath("uwu", "contributors_panel"),
                                        new Padding(
                                            Insets.top(8),
                                            new Sized(
                                                96,
                                                96,
                                                new EntityWidget(1.35, this.displayEntity, widget -> {
                                                    widget.displayMode(displayEntity.isDeadOrDying() ? EntityWidget.DisplayMode.NONE : EntityWidget.DisplayMode.CURSOR);
                                                    if (displayEntity.isDeadOrDying()) {
                                                        widget.transform((matrix) -> {
                                                            if (displayEntity.getUUID().equals(UUID.fromString("91a033f7-1dd3-4858-9c7b-8fb61ba6363d"))) {
                                                                matrix.translate(-1f, 1f, 0);
                                                            }

                                                            matrix.rotateX((float) Math.toRadians(0.01));
                                                        });
                                                    }
                                                })
                                            )
                                        )
                                    )
                                ),
                                new Visibility(
                                    this.dead && this.displayEntity.getUUID().equals(UUID.fromString("91a033f7-1dd3-4858-9c7b-8fb61ba6363d")),
                                    new Stack(
                                        new Transform(
                                            new Matrix3x2f(),
                                            new ItemStackWidget(
                                                SharedState.getWithoutDependency(context, MurderState.class).bed,
                                                widget -> widget
                                                    .displayContext(ItemDisplayContext.NONE)
                                                    .transform(matrix4f -> matrix4f
                                                        .rotate(Axis.YP.rotationDegrees(90))
                                                        .rotate(Axis.ZP.rotationDegrees(15))
                                                        .scale(.45f, .45f, .45f)
                                                        .translate(0, -.45f, .45f))
                                            )
                                        ),
                                        new Align(
                                            Alignment.TOP_LEFT,
                                            new Transform(
                                                new Matrix3x2f().translation(75, 10),
                                                new Label(new LabelStyle(Alignment.TOP_LEFT, null, null, true), true, Component.literal("    z\n  z\nz"))
                                            )
                                        )
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
                        return new Interactable(
                            SHORTCUTS,
                            widget -> widget
                                .addCallbackAction(PrimaryActionIntent.class, ($, $$) -> setState(() -> this.selectedStarCount = idx + 1))
                                .addCallbackAction(SecondaryActionIntent.class, ($, $$) -> setState(() -> this.selectedStarCount = 0))
                                .enterCallback(() -> setState(() -> this.hoverStarCount = idx + 1)),
                            new Stack(
                                new SpriteWidget(
                                    new Material(
                                        Identifier.parse("textures/atlas/gui.png"),
                                        Identifier.fromNamespaceAndPath("uwu", (idx + 1) <= this.selectedStarCount ? "favorite_icon_selected" : "favorite_icon")
                                    )
                                ),
                                (idx + 1) <= this.hoverStarCount
                                    ? new SpriteWidget(
                                    new Material(
                                        Identifier.parse("textures/atlas/gui.png"),
                                        Identifier.fromNamespaceAndPath("uwu", "favorite_icon_hover")
                                    )
                                ) : new Padding(Insets.none())
                            )
                        );
                    }
                }
            }
        }

        private static final Map<List<ShortcutTrigger>, Intent> SHORTCUTS = Map.of(
            List.of(ShortcutTrigger.LEFT_CLICK), PrimaryActionIntent.INSTANCE,
            List.of(ShortcutTrigger.RIGHT_CLICK), SecondaryActionIntent.INSTANCE
        );
    }

    public static class AnimationsTest extends StatefulWidget {
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
                return new State();
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

    public static class Amogus extends StatelessWidget {

        public final Widget bodyPixel;
        public final Widget visorPixel;
        public final double pixelSize;

        public Amogus(Widget bodyPixel, Widget visorPixel, double pixelSize) {
            this.bodyPixel = bodyPixel;
            this.visorPixel = visorPixel;
            this.pixelSize = pixelSize;
        }

        @Override
        public Widget build(BuildContext context) {
            return new Sized(
                this.pixelSize * 4,
                this.pixelSize * 4,
                new Grid(
                    LayoutAxis.VERTICAL,
                    4,
                    Grid.CellFit.tight(),
                    (Widget) null,
                    this.bodyPixel,
                    this.bodyPixel,
                    this.bodyPixel,
                    this.bodyPixel,
                    this.bodyPixel,
                    this.visorPixel,
                    this.visorPixel,
                    this.bodyPixel,
                    this.bodyPixel,
                    this.bodyPixel,
                    this.bodyPixel,
                    null,
                    this.bodyPixel,
                    null,
                    this.bodyPixel
                )
            );
        }
    }

    public static class NavigatorTest extends StatelessWidget {
        @Override
        public Widget build(BuildContext context) {
            return new Center(
                new Sized(
                    150, 150,
                    new Box(
                        Color.BLACK, true,
                        new Padding(
                            Insets.all(1),
                            new Center(new Navigator(new Page1()))
                        )
                    )
                )
            );
        }

        public static class Page1 extends StatelessWidget {
            @Override
            public Widget build(BuildContext context) {
                return new Column(
                    new MessageButton(
                        Component.literal("page 1"),
                        () -> Navigator.push(context, new BasePage(new Label(Component.literal("page 1"))))
                    ),
                    new MessageButton(
                        Component.literal("page 2"),
                        () -> Navigator.push(
                            context, new BasePage(
                                new Column(
                                    new Label(Component.literal("page 2")),
                                    new MessageButton(
                                        Component.literal("popup"),
                                        () -> Navigator.pushOverlay(
                                            context, new Dialog(
                                                new Panel(
                                                    Panel.VANILLA_LIGHT,
                                                    new Padding(
                                                        Insets.all(5),
                                                        new Sized(
                                                            Size.square(64),
                                                            new Bikeshed()
                                                        )
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

        public static class BasePage extends StatelessWidget {

            public final Widget content;

            public BasePage(Widget content) {
                this.content = content;
            }

            @Override
            public Widget build(BuildContext context) {
                return new Column(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    this.content,
                    new Padding(
                        Insets.top(10),
                        new MessageButton(Component.literal("go back"), () -> Navigator.pop(context))
                    )
                );
            }
        }
    }

    public static class OverlayTest extends StatefulWidget {
        @Override
        public WidgetState<OverlayTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<OverlayTest> {

            private @Nullable Tests selectedOption = null;

            private void spawn(BuildContext context, double x, double y) {
                Overlay.of(context).add(
                    new OverlayEntryBuilder(
                        new Amogus(
                            new Box(Color.randomHue()),
                            new Box(Color.WHITE),
                            8
                        ),
                        new RelativePosition(context, x - 12, y - 12)
                    )
                );
            }

            @Override
            public Widget build(BuildContext context) {
                return new Overlay(
                    new Builder(innerContext -> {
                        return new Sized(
                            Double.POSITIVE_INFINITY,
                            Double.POSITIVE_INFINITY,
                            new MouseArea(
                                widget -> widget
                                    .clickCallback((x, y, button, modifiers) -> {
                                        this.spawn(innerContext, x, y);
                                        return true;
                                    })
                                    .dragCallback((x, y, dx, dy) -> {
                                        this.spawn(innerContext, x, y);
                                    }),
                                new Center(
                                    new Panel(
                                        Panel.VANILLA_LIGHT,
                                        new Padding(
                                            Insets.all(10),
                                            new Sized(
                                                120,
                                                null,
                                                new ComboBox<>(
                                                    test -> Component.literal(test.name().toLowerCase(Locale.ROOT).replace('_', ' ')),
                                                    Arrays.asList(Tests.values()),
                                                    this.selectedOption,
                                                    option -> this.setState(() -> this.selectedOption = option)
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        );
                    })
                );
            }
        }
    }

    public static class TextTest extends StatefulWidget {
        @Override
        public WidgetState<TextTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<TextTest> {

            @Override
            public Widget build(BuildContext context) {
                var wisdomText = Component.literal(String.join(" ", Wisdom.ALL_THE_WISDOM));

                return new DragArena(
                    new Window(
                        false,
                        Component.literal("ellipsis moment"),
                        null, null,
                        Size.square(100),
                        new Label(
                            new LabelStyle(Alignment.TOP_LEFT, null, null, null),
                            true, Label.Overflow.ELLIPSIS,
                            wisdomText
                        )
                    ),
                    new Window(
                        false,
                        Component.literal("clip moment"),
                        null, null,
                        Size.square(100),
                        new Label(
                            new LabelStyle(Alignment.TOP_LEFT, null, null, null),
                            true, Label.Overflow.CLIP,
                            wisdomText
                        )
                    ),
                    new Window(
                        false,
                        Component.literal("marquee moment"),
                        null, null,
                        Size.square(100),
                        new Column(
                            Wisdom.ALL_THE_WISDOM.stream()
                                .sorted(Comparator.comparingInt(value -> Minecraft.getInstance().font.width(value)))
                                .map(s -> new Marquee(
                                    new Label(
                                        new LabelStyle(Alignment.TOP_LEFT, null, null, null),
                                        true, Label.Overflow.CLIP,
                                        Component.literal(s)
                                    )
                                )).toList()
                        )
                    ),
                    new Window(
                        false,
                        Component.literal("cursed marquee moment"),
                        null, null,
                        Size.square(100),
                        new Marquee(
                            widget -> widget.pauseWhileHovered(false),
                            new Bikeshed()
                        )
                    ),
                    new Window(
                        false,
                        Component.literal("dvd moment"),
                        null, null,
                        Size.square(100),
                        new Center(
                            new Marquee(
                                widget -> widget.axis(LayoutAxis.VERTICAL),
                                new Marquee(
                                    new Panel(
                                        Identifier.fromNamespaceAndPath("uwu", "contributors_panel"),
                                        new Sized(
                                            32 * 4,
                                            32 * 4,
                                            new Marquee(
                                                widget -> widget
                                                    .easing(Easing.LINEAR)
                                                    .minDuration(0)
                                                    .durationPerPixel(10)
                                                    .pauseTime(0),
                                                new Marquee(
                                                    widget -> widget
                                                        .easing(Easing.LINEAR)
                                                        .minDuration(0)
                                                        .durationPerPixel(15)
                                                        .pauseTime(0)
                                                        .axis(LayoutAxis.VERTICAL),
                                                    new Align(
                                                        Alignment.TOP_LEFT,
                                                        new Padding(
                                                            Insets.all(32 * 3),
                                                            new GayAmogus(
                                                                8
                                                            )
                                                        )
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

    public static class GayAmogus extends StatefulWidget {

        public final double pixelSize;
        public GayAmogus(double pixelSize) {
            this.pixelSize = pixelSize;
        }

        @Override
        public WidgetState<GayAmogus> createState() {
            return new State();
        }

        public static class State extends WidgetState<GayAmogus> {

            @Override
            public void init() {
                this.update(Duration.ZERO);
            }

            private void update(Duration delta) {
                this.setState(() -> {});
                this.scheduleAnimationCallback(this::update);
            }

            @Override
            public Widget build(BuildContext context) {
                return new Amogus(
                    new Box(Color.hsv(System.currentTimeMillis() / 5000d % 1d, .85, 1)),
                    new Box(Color.WHITE),
                    this.widget().pixelSize
                );
            }
        }
    }

    public static class SpinnyGhastTest extends StatefulWidget {
        @Override
        public WidgetState<SpinnyGhastTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<SpinnyGhastTest> {

            private List<Entity> entities;
            private int selectedEntityIdx = 0;

            private double pitch = 35;
            private double pitchDelta = 0;

            private double yaw = -45;
            private double yawDelta = 0;

            private boolean releasedLate = false;

            @Override
            public void init() {
                this.entities = Stream.of(
                    EntityType.HAPPY_GHAST,
                    EntityType.ALLAY,
                    EntityType.COW,
                    EntityType.CREAKING,
                    EntityType.BREEZE,
                    EntityType.COPPER_GOLEM
                ).<Entity>map(
                    entityType -> entityType.create(Minecraft.getInstance().level, EntitySpawnReason.MOB_SUMMONED)
                ).toList();
            }

            private void animate(Duration delta) {
                var seconds = delta.toNanos() / (double) Duration.ofSeconds(1).toNanos();
                this.pitch += this.pitchDelta * seconds * 10;
                this.yaw += this.yawDelta * seconds * 10;

                this.pitchDelta += Delta.compute(this.pitchDelta, 0, seconds * .5);
                this.yawDelta += Delta.compute(this.yawDelta, 0, seconds * .5);

                if (Math.abs(this.pitchDelta) > 1e-4 || Math.abs(this.yawDelta) > 1e-4) {
                    this.scheduleAnimationCallback(this::animate);
                }
            }

            @Override
            public Widget build(BuildContext context) {
                return new Row(
                    MainAxisAlignment.CENTER,
                    CrossAxisAlignment.CENTER,
                    new Padding(Insets.all(10)),
                    List.of(
                        new VerticalCarouselThing(
                            this.selectedEntityIdx,
                            idx -> this.setState(() -> this.selectedEntityIdx = idx),
                            Size.square(64), this.entities.stream().<Widget>map(entity -> new EntityWidget(.75, entity, null)).toList()),
                        new MouseArea(
                            widget -> widget
                                .clickCallback((x, y, button, modifiers) -> {
                                    this.yawDelta = 0;
                                    this.pitchDelta = 0;

                                    return true;
                                })
                                .dragCallback((x, y, dx, dy) -> {
                                    this.yaw += dx * .5;
                                    this.pitch += dy * .5;

                                    this.yawDelta = dx;
                                    this.pitchDelta = dy;

                                    this.releasedLate = false;
                                    this.scheduleDelayedCallback(Duration.ofMillis(200), () -> {
                                        this.releasedLate = true;
                                    });
                                })
                                .dragEndCallback(() -> {
                                    if (this.releasedLate) return;
                                    this.animate(Duration.ZERO);
                                }),
                            new Sized(
                                Size.square(400),
                                new EntityWidget(
                                    .75,
                                    this.entities.get(this.selectedEntityIdx),
                                    widget -> widget
                                        .displayMode(EntityWidget.DisplayMode.NONE)
                                        .transform(matrix4f -> {
                                            matrix4f.rotateX((float) Math.toRadians(this.pitch));
                                            matrix4f.rotateY((float) Math.toRadians(this.yaw));
                                        })
                                )
                            )
                        )
                    )
                );
            }
        }

        public static class VerticalCarouselThing extends StatefulWidget {

            public final int selectedIndex;
            public final IntConsumer onChanged;
            public final Size itemSize;
            public final List<Widget> children;

            public VerticalCarouselThing(int selectedIndex, IntConsumer onChanged, Size itemSize, List<Widget> children) {
                this.selectedIndex = selectedIndex;
                this.onChanged = onChanged;
                this.itemSize = itemSize;
                this.children = children;
            }

            @Override
            public WidgetState<VerticalCarouselThing> createState() {
                return new State();
            }

            public static class State extends WidgetState<VerticalCarouselThing> {

                @Override
                public Widget build(BuildContext context) {
                    var displayChildren = new ArrayList<Widget>();
                    displayChildren.add(new Panel(Panel.VANILLA_INSET));

                    var offset = -this.widget().selectedIndex * this.widget().itemSize.height() / 2;
                    offset -= this.widget().itemSize.height() / 4;

                    for (var i = 0; i < this.widget().children.size(); i++) {
                        var thisOffset = (float) offset;

                        var scale = i == this.widget().selectedIndex ? 1 : .5f;
                        offset += this.widget().itemSize.height() * scale;

                        if (scale == 1) {
                            //noinspection lossy-conversions
                            thisOffset += this.widget().itemSize.height() / 4f;
                        }

                        var elementIndex = i;
                        displayChildren.add(
                            new Transform(
                                new Matrix3x2f()
                                    .translate(0f, thisOffset)
                                    .scale(scale, scale),
                                Interactable.primary(
                                    () -> this.widget().onChanged.accept(elementIndex),
                                    this.widget().children.get(i)
                                )
                            ));
                    }

                    return new MouseArea(
                        widget -> widget
                            .scrollCallback((horizontal, vertical) -> {
                                if (vertical == 0) return false;
                                this.setState(() -> {
                                    this.widget().onChanged.accept(Mth.clamp(
                                        this.widget().selectedIndex - (int) Math.signum(vertical),
                                        0,
                                        this.widget().children.size() - 1
                                    ));
                                });

                                return true;
                            }),
                        new Row(
                            MainAxisAlignment.CENTER,
                            CrossAxisAlignment.CENTER,
                            new Sized(
                                Size.of(18, this.widget().itemSize.height()),
                                new Grid(
                                    LayoutAxis.HORIZONTAL,
                                    2,
                                    Grid.CellFit.tight(),
                                    new MessageButton(Component.literal("↑"), this.widget().selectedIndex > 0 ? () -> this.widget().onChanged.accept(this.widget().selectedIndex - 1) : null),
                                    new MessageButton(Component.literal("↓"), this.widget().selectedIndex < this.widget().children.size() - 1 ? () -> this.widget().onChanged.accept(this.widget().selectedIndex + 1) : null)
                                )
                            ),
                            new Sized(
                                this.widget().itemSize,
                                new Stack(
                                    displayChildren
                                )
                            )
                        )
                    );
                }
            }
        }
    }

    public static class OptimizationTest extends StatelessWidget {

        @Override
        public Widget build(BuildContext context) {
            var widget = new Sized(
                128, 128,
                new EntityWidget(
                    1.5, BurningChyz.of(context),
                    entityWidget -> entityWidget.displayMode(EntityWidget.DisplayMode.CURSOR)
                )
            );
            return new VerticallyScrollable(
                new Grid(
                    LayoutAxis.VERTICAL,
                    32,
                    Grid.CellFit.loose(),
                    Stream.generate(() -> widget).limit(32 * 32).toList()
                )
            );
        }
    }

    public static class AutomaticAnimationTest extends StatefulWidget {
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
            public State createState() {
                return new State();
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

    public static class KdlWidgetsTest extends StatefulWidget {
        @Override
        public WidgetState<KdlWidgetsTest> createState() {
            return new State();
        }

        public static class State extends WidgetState<KdlWidgetsTest> {

            private TextEditingController textController;

            private KdlNode rootNode;
            private Widget kdlWidget = EmptyWidget.INSTANCE;

            @Override
            public void init() {
                this.textController = new TextEditingController();
                this.textController.addListener(() -> {
                    try {
                        var parsedKdl = new Kdl2Parser().parse(this.textController.value().text());
                        this.rootNode = parsedKdl.nodes().getFirst();

                        var deserializer = new KdlDeserializer(this.rootNode, KdlMapper.DEFAULT_MAPPERS);
                        var ctx = deserializer.setupContext(SerializationContext.attributes(
                            SerializationAttributes.HUMAN_READABLE,
                            BraidKdlEndecs.HANDLERS.instance(Map.of("lmao", (theArg) -> System.out.println("lmao: " + theArg)))
                        ));

                        var parsedWidget = WidgetEndec.ROOT.decode(ctx, deserializer);
                        this.setState(() -> {
                            this.kdlWidget = parsedWidget;
                        });
                    } catch (Exception e) {
                        Throwable cause = e;
                        while (cause.getCause() != null) {
                            cause = cause.getCause();
                        }

                        var bruhJava = cause;
                        this.setState(() -> {
                            this.kdlWidget = new Label(LabelStyle.SHADOW, true, Component.literal(Objects.requireNonNullElse(bruhJava.getMessage(), "no message")).withStyle(ChatFormatting.RED));
                        });
                    }
                });
            }

            private void showJson() {
                var deserializer = new KdlDeserializer(this.rootNode, KdlMapper.DEFAULT_MAPPERS);
                var jsonOut = GsonSerializer.of();

                deserializer.readAny(SerializationContext.attributes(SerializationAttributes.HUMAN_READABLE), jsonOut);
                var jsonText = new GsonBuilder().setPrettyPrinting().create().toJson(jsonOut.result());

                var widget = new Box(
                    Color.mix(.1, Color.BLACK, Color.WHITE),
                    new VerticallyScrollable(
                        null,
                        ScrollAnimationSettings.DEFAULT,
                        new Padding(
                            Insets.all(5),
                            new Label(
                                new LabelStyle(Alignment.TOP_LEFT, null, Style.EMPTY.withFont(new FontDescription.Resource(Minecraft.UNIFORM_FONT)), false),
                                true,
                                Component.literal(jsonText)
                            )
                        )
                    )
                );

                BraidWindow.open(
                    "json preview",
                    650, 650,
                    widget
                );
            }

            @Override
            public Widget build(BuildContext context) {
                return new Row(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new Column(
                        MainAxisAlignment.START,
                        CrossAxisAlignment.END,
                        new Sized(
                            350, 300,
                            new TextBox(
                                this.textController,
                                widget -> widget
                                    .placeholder(Component.literal("KDL goes here"))
                                    .softWrap(false)
                            )
                        ),
                        new MessageButton(
                            Component.literal("view as json"),
                            this::showJson
                        )
                    ),
                    new Sized(
                        Size.square(250),
                        this.kdlWidget
                    )
                );
            }
        }
    }

//    public static class BeegGrid extends StatefulWidget {
//        @Override
//        public WidgetState<BeegGrid> createState() {
//            return new State();
//        }
//
//        public static class State extends WidgetState<BeegGrid> {
//
//            private List<String> lines;
//
//            @Override
//            public void init() {
//                try {
//                    this.lines = Files.readAllLines(Path.of("sounds.json"));
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            @Override
//            public Widget build(BuildContext context) {
//                var children = new ArrayList<Widget>();
//
//                for (var lineIdx = 0; lineIdx < this.lines.size(); lineIdx++) {
//                    var line = this.lines.get(lineIdx);
//
//                    children.add(new Label(Text.literal(String.valueOf(lineIdx))));
//                    children.add(new Label(Text.literal(line)));
//                }
//
//                return new VerticallyScrollable(
//                    new Grid(
//                        LayoutAxis.VERTICAL,
//                        2,
//                        Grid.CellFit.loose(Alignment.LEFT),
//                        children
//                    )
//                );
//            }
//        }
//    }
}
