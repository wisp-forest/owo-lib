package io.wispforest.owo.samples.braid;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.Flexible;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.scroll.ScrollAnimationSettings;
import io.wispforest.owo.braid.widgets.scroll.VerticallyScrollable;
import io.wispforest.owo.samples.braid.layout.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class LayoutWidgetExamples extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        var examples = List.of(
            new Example(
                "Padding",
                new PaddedLogo()
            ),
            new Example(
                "Grow and align",
                new BottomRightLogo()
            ),
            new Example(
                "Align with size factors",
                new SizeFactorLogo()
            ),
            new Example(
                "Sized",
                new SquishedLogo()
            ),
            new Example(
                "Constrain",
                new LargeLogo()
            ),
            new Example(
                "Vertical Flex",
                new VerticalFlex()
            ),
            new Example(
                "Row",
                new NormalRow()
            ),
            new Example(
                "Column",
                new PaddedColumn()
            ),
            new Example(
                "Grid",
                new Checkerboard()
            ),
            new Example(
                "Stack",
                new RGBStack()
            ),
            new Example(
                "Stack with base",
                new LavaLogo()
            )
        );

        return new LayoutBuilder(
            (builderContext, constraints) -> {
                var crossAxisCells = Mth.floor(constraints.maxWidth() / 150);
                return new VerticallyScrollable(
                    null, ScrollAnimationSettings.DEFAULT,
                    new Align(
                        Alignment.TOP,
                        new Grid(
                            LayoutAxis.VERTICAL,
                            crossAxisCells,
                            Grid.CellFit.loose(),
                            widget -> new Padding(
                                Insets.all(5),
                                new Box(
                                    Color.BLACK.withA(.25),
                                    new Padding(
                                        Insets.all(5),
                                        new AspectRatio(
                                            1,
                                            widget
                                        )
                                    )
                                )
                            ),
                            examples
                        )
                    )
                );
            }
        );
    }

    public static class Example extends StatelessWidget {

        public final String name;
        public final Widget child;

        public Example(String name, Widget child) {
            this.name = name;
            this.child = child;
        }

        @Override
        public Widget build(BuildContext context) {
            return new Column(
                new Flexible(
                    new Center(
                        this.child
                    )
                ),
                new Label(LabelStyle.SHADOW, true, Component.literal(this.name))
            );
        }
    }
}
