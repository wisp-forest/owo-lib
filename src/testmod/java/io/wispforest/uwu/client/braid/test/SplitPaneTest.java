package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.InspectorProperty;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.slider.slider.MessageSlider;
import io.wispforest.owo.braid.widgets.splitpane.*;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SplitPaneTest extends StatefulWidget {
    private static final double TEST_WIDTH = 180;
    private static final double TEST_HEIGHT = 140;

    private static Widget pane(Color color, String label) {
        return new Box(Color.mix(0.5, color, new Color(0)), Label.literal(label));
    }

    @Override
    public WidgetState<SplitPaneTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<SplitPaneTest> {
        private static final ResizeDistribution[] DISTRIBUTIONS = {ResizeDistribution.ALL, ResizeDistribution.FIRST, ResizeDistribution.LAST, ResizeDistribution.LARGEST, ResizeDistribution.SMALLEST};
        private static final String[] DISTRIBUTION_NAMES = {"ALL", "FIRST", "LAST", "LARGEST", "SMALLEST"};

        private int overflowIndex = 3;
        private int underflowIndex = 4;
        private double dividerThickness = 1;
        private boolean pushDividers = false;
        private boolean preserveSizes = true;

        private final SplitController sharedGridController = new SplitController(2);
        private final SplitController fiveLayerController = new SplitController(2);

        @Override
        public Widget build(BuildContext context) {
            var style = new SplitPaneStyle(dividerThickness, null, DISTRIBUTIONS[overflowIndex], DISTRIBUTIONS[underflowIndex], preserveSizes, pushDividers);

            return new Stack(
                Alignment.CENTER,
                new Grid(
                    LayoutAxis.VERTICAL, 3, Grid.CellFit.loose(Alignment.TOP_LEFT),
                    new Column(
                        Label.literal("Horizontal"),
                        new Sized(
                            TEST_WIDTH, TEST_HEIGHT, new SplitPane(
                            LayoutAxis.HORIZONTAL, style,
                            pane(Color.GREEN, "1"),
                            pane(Color.GREEN, "2"),
                            pane(Color.GREEN, "3"),
                            pane(Color.GREEN, "4")
                        )
                        )
                    ),
                    new Column(
                        Label.literal("Vertical"),
                        new Sized(
                            TEST_WIDTH, TEST_HEIGHT, new SplitPane(
                            LayoutAxis.VERTICAL, style,
                            pane(Color.BLUE, "top"),
                            pane(Color.BLUE, "mid"),
                            pane(Color.BLUE, "bot")
                        )
                        )
                    ),
                    new Column(
                        Label.literal("Weighted"),
                        new Sized(
                            TEST_WIDTH, TEST_HEIGHT, new SplitPane(
                            LayoutAxis.HORIZONTAL, style,
                            new SplitChild(sc -> sc.weight(1), pane(Color.YELLOW, "w1")),
                            new SplitChild(sc -> sc.weight(2), pane(Color.YELLOW, "w2")),
                            new SplitChild(sc -> sc.weight(1), pane(Color.YELLOW, "w1"))
                        )
                        )
                    ),
                    new Column(
                        Label.literal("Constrained"),
                        new Sized(
                            TEST_WIDTH, TEST_HEIGHT, new SplitPane(
                            LayoutAxis.HORIZONTAL, style,
                            new SplitChild(sc -> sc.minSize(50), pane(Color.YELLOW, "min 50")),
                            new SplitChild(sc -> sc.maxSize(100), pane(Color.YELLOW, "max 100")),
                            pane(Color.YELLOW, "free")
                        )
                        )
                    ),
                    new Column(
                        Label.literal("Over/Under Flow"),
                        new Sized(
                            TEST_WIDTH, TEST_HEIGHT, new SplitPane(
                            LayoutAxis.HORIZONTAL, style,
                            new SplitPane(
                                LayoutAxis.HORIZONTAL, style,
                                new SplitChild(sc -> sc.size(20), pane(Color.RED, "fix 20")),
                                new SplitChild(sc -> sc.size(60), pane(Color.RED, "fix 60")),
                                new SplitChild(sc -> sc.size(40), pane(Color.RED, "fix 40"))
                            ),
                            new SplitChild(sc -> sc.size(60), pane(Color.YELLOW, "←"))
                        )
                        )
                    ),
                    new Column(
                        Label.literal("Mixed Sizing"),
                        new Sized(
                            TEST_WIDTH, TEST_HEIGHT, new SplitPane(
                            LayoutAxis.HORIZONTAL, style,
                            new SplitPane(
                                LayoutAxis.HORIZONTAL, style,
                                new SplitChild(sc -> sc.size(60), pane(Color.RED, "fix 60")),
                                pane(Color.RED, "flex"),
                                pane(Color.RED, "flex"),
                                new SplitChild(sc -> sc.size(50), pane(Color.RED, "fix 50"))
                            ),
                            new SplitChild(sc -> sc.weight(0.1), pane(Color.YELLOW, "←"))
                        )
                        )
                    ),
                    new Column(
                        Label.literal("Nested"),
                        new Sized(
                            TEST_WIDTH, TEST_HEIGHT, new SplitPane(
                            LayoutAxis.HORIZONTAL, style,
                            new SplitPane(
                                LayoutAxis.VERTICAL, style,
                                pane(Color.AQUA, "1"),
                                new SplitChild(sc -> sc.weight(2), pane(Color.AQUA, "2"))
                            ),
                            new SplitPane(
                                LayoutAxis.VERTICAL, style,
                                new SplitChild(sc -> sc.weight(2), pane(Color.MAGENTA, "3")),
                                pane(Color.MAGENTA, "4")
                            )
                        )
                        )
                    ),
                    new Column(
                        Label.literal("Shared Controller"),
                        new Sized(
                            TEST_WIDTH, TEST_HEIGHT,
                            new SplitPane(
                                LayoutAxis.HORIZONTAL, style,
                                new SplitPane(
                                    LayoutAxis.VERTICAL, sharedGridController, style,
                                    pane(Color.RED, "1"),
                                    pane(Color.RED, "2")
                                ),
                                new SplitPane(
                                    LayoutAxis.VERTICAL, sharedGridController, style,
                                    pane(Color.GREEN, "3"),
                                    pane(Color.GREEN, "4")
                                )
                            )
                        )
                    ),
                    new Column(
                        Label.literal("Funni"),
                        new Sized(
                            TEST_WIDTH, TEST_HEIGHT,
                            new SplitPane(
                                LayoutAxis.HORIZONTAL, fiveLayerController, style,
                                pane(Color.BLUE, "1"),
                                new SplitPane(
                                    LayoutAxis.VERTICAL, fiveLayerController, style,
                                    pane(Color.YELLOW, "2"),
                                    new SplitPane(
                                        LayoutAxis.HORIZONTAL, fiveLayerController, style,
                                        pane(Color.RED, "3"),
                                        new SplitPane(
                                            LayoutAxis.VERTICAL, fiveLayerController, style,
                                            pane(Color.GREEN, "4"),
                                            new SplitPane(
                                                LayoutAxis.HORIZONTAL, fiveLayerController, style,
                                                pane(Color.AQUA, "5"),
                                                pane(Color.AQUA, "6")
                                            )
                                        )
                                    )
                                )
                            )
                        )
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
                                    new IntrinsicWidth(
                                        new Column(
                                            MainAxisAlignment.START,
                                            CrossAxisAlignment.STRETCH,
                                            new MessageButton(
                                                Component.literal("Overflow: " + DISTRIBUTION_NAMES[overflowIndex]),
                                                () -> setState(() -> overflowIndex = (overflowIndex + 1) % DISTRIBUTIONS.length)
                                            ),
                                            new MessageButton(
                                                Component.literal("Underflow: " + DISTRIBUTION_NAMES[underflowIndex]),
                                                () -> setState(() -> underflowIndex = (underflowIndex + 1) % DISTRIBUTIONS.length)
                                            ),
                                            new Sized(
                                                null, 20, new MessageSlider(
                                                dividerThickness,
                                                Component.literal("Divider: " + InspectorProperty.rounded(dividerThickness)),
                                                slider -> slider.range(1, 12).step(1),
                                                v -> setState(() -> dividerThickness = v)
                                            )
                                            ),
                                            new MessageButton(
                                                Component.literal("Preserve: " + preserveSizes),
                                                () -> setState(() -> preserveSizes = !preserveSizes)
                                            ),
                                            new MessageButton(
                                                Component.literal("Push: " + pushDividers),
                                                () -> setState(() -> pushDividers = !pushDividers)
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
