package io.wispforest.uwu.client.braid;

import com.mojang.authlib.GameProfile;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.braid.animation.*;
import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.util.BraidToast;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.*;
import io.wispforest.owo.braid.widgets.checkbox.Checkbox;
import io.wispforest.owo.braid.widgets.checkbox.CheckboxStyle;
import io.wispforest.owo.braid.widgets.checkbox.DefaultCheckboxStyle;
import io.wispforest.owo.braid.widgets.cycle.MessageCyclingButton;
import io.wispforest.owo.braid.widgets.flex.*;
import io.wispforest.owo.braid.widgets.focus.FocusPolicy;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import io.wispforest.owo.braid.widgets.sharedstate.ShareableState;
import io.wispforest.owo.braid.widgets.sharedstate.SharedStateStorage;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.intents.*;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.object.entity.EntityDisplayMode;
import io.wispforest.owo.braid.widgets.object.entity.EntityWidget;
import io.wispforest.owo.braid.widgets.scroll.*;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.braid.widgets.slider.Incrementor;
import io.wispforest.owo.braid.widgets.slider.slider.MessageSlider;
import io.wispforest.owo.braid.widgets.slider.xlyder.MessageXlyder;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.util.EventSource;
import io.wispforest.uwu.client.HudTestWidget;
import io.wispforest.uwu.client.braid.test.*;
import io.wispforest.uwu.client.braid.test.FlexTest;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.wispforest.owo.braid.framework.instance.InspectorProperty.rounded;


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

    public static class State extends WidgetState<TestSelector> {

        private double xSkew = 0f;
        private double ySkew = 0f;

        private double rotat = 0f;
        private int fliptat = 0;
        private boolean bouncy = false;

        private Player chyz;

        private boolean disableEverything = false;

        @Override
        public void init() {
            this.chyz = EntityComponent.createRenderablePlayer(new GameProfile(
                UUID.fromString("09de8a6d-86bf-4c15-bb93-ce3384ce4e96"),
                "chyzman"
            ));

            this.chyz.setSharedFlagOnFire(true);
        }

        @Override
        public Widget build(BuildContext ctx) {
            return new SharedState<>(
                GlobalStateTest.INSTANCE::self, new Builder(outerCtx -> {
                var useGlobal = SharedState.get(outerCtx, GlobalStateTest.class);
                return new SharedState<>(
                    useGlobal.global ? SelectedTest.INSTANCE::self : SelectedTest::new,
                new Builder(context -> {
                    var selectedTest = SharedState.get(context, SelectedTest.class);
                    var buttons = Arrays.stream(Tests.values()).map(test -> {
                        if (test == Tests.BURNING_CHYZ) {
                            return new BurningChyzButton(this.chyz, () -> selectedTest.setState(() -> selectedTest.selectedTest = Tests.BURNING_CHYZ));
                        } else {
                            return new MessageButton(
                                Component.literal(test.name().toLowerCase(Locale.ROOT).replace('_', ' ')),
                                selectedTest.selectedTest != test ? () -> selectedTest.setState(() -> selectedTest.selectedTest = test) : null
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

                    return new ControlsOverride(
                        disableEverything,
                        new DefaultCheckboxStyle(
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
                                                    switch (selectedTest.selectedTest) {
                                                        case COUNTER -> new CounterTest();
                                                        case FLEX -> new FlexTest();
                                                        case DRAGGING -> new DragArenaTest();
                                                        case SPLIT_PANE -> new SplitPaneTest();
                                                        case SLIDERS -> new SliderTest();
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
                                                        case null -> new Center(Label.literal("select a test"));
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
                                                new ControlsOverride(
                                                    false,
                                                    new Row(
                                                        MainAxisAlignment.SPACE_EVENLY,
                                                        CrossAxisAlignment.CENTER,
                                                        Label.literal("Disabled"),
                                                        new Checkbox(
                                                            CheckboxStyle.BRAID,
                                                            this.disableEverything,
                                                            checked -> this.setState(() -> this.disableEverything = checked)
                                                        )
                                                    )
                                                ),
                                                new ControlsOverride(
                                                    false,
                                                    new Row(
                                                        MainAxisAlignment.SPACE_EVENLY,
                                                        CrossAxisAlignment.CENTER,
                                                        Label.literal("Global"),
                                                        new Checkbox(
                                                            CheckboxStyle.BRAID,
                                                            useGlobal.global,
                                                            checked -> useGlobal.setState(() -> useGlobal.global = checked)
                                                        )
                                                    )
                                                ),
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
                                                                () -> Actions.invoke(
                                                                    Focusable.of(context).primaryFocus().context(),
                                                                    new Incrementor.IncrementIntent(LayoutAxis.VERTICAL, 1)
                                                                )
                                                            ),
                                                            new MessageButton(
                                                                Component.literal("↓"),
                                                                () -> Actions.invoke(
                                                                    Focusable.of(context).primaryFocus().context(),
                                                                    new Incrementor.IncrementIntent(LayoutAxis.VERTICAL, -1)
                                                                )
                                                            ),
                                                            new MessageButton(
                                                                Component.literal("←"),
                                                                () -> Actions.invoke(
                                                                    Focusable.of(context).primaryFocus().context(),
                                                                    new Incrementor.IncrementIntent(LayoutAxis.HORIZONTAL, -1)
                                                                )
                                                            ),
                                                            new MessageButton(
                                                                Component.literal("→"),
                                                                () -> Actions.invoke(
                                                                    Focusable.of(context).primaryFocus().context(),
                                                                    new Incrementor.IncrementIntent(LayoutAxis.HORIZONTAL, 1)
                                                                )
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
                                                            Label.literal("fliptat:")
                                                        ),
                                                        new Row(
                                                            MainAxisAlignment.START,
                                                            CrossAxisAlignment.CENTER,
                                                            new Flexible(
                                                                new MessageButton(Component.literal("-"), () -> this.setState(() -> this.fliptat -= 1))
                                                            ),
                                                            new Flexible(
                                                                Label.literal(String.valueOf(this.fliptat))
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
                                                        Component.literal("rotat: " + rounded(this.rotat)), widget -> widget.range(0, 360).incrementStep(1),
                                                        value -> this.setState(() -> this.rotat = value)
                                                    )
                                                ),
                                                new Sized(
                                                    75.0,
                                                    75.0,
                                                    new MessageXlyder(
                                                        this.xSkew,
                                                        this.ySkew,
                                                        Component.literal("x skew: " + (rounded(this.xSkew)) + "\ny skew: " + (rounded(this.ySkew))),
                                                        xlyder -> xlyder.range(-.75, .75),

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
                        )
                    );
                })
            );
            }));
        }
    }

    public static class SelectedTest extends ShareableState {
        public @Nullable Tests selectedTest = null;

        private static final Endec<SelectedTest> ENDEC = StructEndecBuilder.of(
            Endec.forEnum(Tests.class).nullableOf()
                .optionalFieldOf("selected_test", s -> s.selectedTest, (Tests) null),
            selectedTest -> {
                var state = new SelectedTest();
                state.selectedTest = selectedTest;
                return state;
            }
        );

        public static final SelectedTest INSTANCE = SharedStateStorage.persist(
            "uwu", "test_selector",
            ENDEC,
            SelectedTest::new
        );
    }

    public static class GlobalStateTest extends ShareableState {
        public boolean global = false;

        private static final Endec<GlobalStateTest> ENDEC = StructEndecBuilder.of(
            Endec.BOOLEAN.fieldOf("use_global", s -> s.global),
            useGlobal -> {
                var state = new GlobalStateTest();
                state.global = useGlobal;
                return state;
            }
        );

        public static final GlobalStateTest INSTANCE = SharedStateStorage.persist(
            "uwu", "test_selector_global",
            ENDEC,
            GlobalStateTest::new
        );
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
                                    widget -> widget.displayMode(EntityDisplayMode.CURSOR)
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
