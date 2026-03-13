package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.Button;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.label.Label;
import net.minecraft.network.chat.Component;

public class GridsTest extends StatelessWidget {
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
