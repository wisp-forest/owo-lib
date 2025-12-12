package io.wispforest.owo.braid.widgets.splitpane;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.Key;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Constrain;
import io.wispforest.owo.braid.widgets.basic.LayoutBuilder;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Flex;
import io.wispforest.owo.braid.widgets.flex.Flexible;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import net.minecraft.util.Mth;

public class SplitPane extends StatefulWidget {

    public final Widget firstChild;
    public final Widget secondChild;
    public final LayoutAxis axis;

    public SplitPane(Widget firstChild, Widget secondChild, LayoutAxis axis) {
        this.firstChild = firstChild;
        this.secondChild = secondChild;
        this.axis = axis;
    }

    @Override
    public WidgetState<SplitPane> createState() {
        return new SplitPaneState();
    }
}

class SplitPaneState extends WidgetState<SplitPane> {

    private double splitCoordinate = -1;

    @Override
    public Widget build(BuildContext context) {
        return new LayoutBuilder((innerContext, constraints) -> {
            var axis = this.widget().axis;
            var maxSize = constraints.maxOnAxis(axis) - 2;

            if (this.splitCoordinate == -1) this.splitCoordinate = .5 * maxSize;
            var split = Math.floor(Mth.clamp(this.splitCoordinate, .1 * maxSize, .9 * maxSize));

            var firstConstraints = Constraints.tight(axis.createSize(split, constraints.maxOnAxis(axis.opposite())));
            var secondConstraints = Constraints.tight(axis.createSize(maxSize - split, constraints.maxOnAxis(axis.opposite())));

            return new Flex(
                axis,
                MainAxisAlignment.START,
                CrossAxisAlignment.START,
                new Constrain(firstConstraints, this.widget().firstChild).key(this.widget().firstChild.key()),
                new Flexible(
                    new MouseArea(
                        widget -> widget
                            .dragCallback((x, y, dx, dy) -> setState(() -> {
                                this.splitCoordinate = this.splitCoordinate + axis.choose(dx, dy);
                                System.out.println("Split coordinate: " + this.splitCoordinate);
                            }))
                            .dragEndCallback(() -> {
                                this.splitCoordinate = Mth.clamp(this.splitCoordinate, .1 * maxSize, .9 * maxSize);
                            })
                            .cursorStyleSupplier((x, y) -> axis.choose(CursorStyle.HORIZONTAL_RESIZE, CursorStyle.VERTICAL_RESIZE)),
                        new Box(Color.WHITE)
                    )
                ).key(Key.of("splitter")),
                new Constrain(secondConstraints, this.widget().secondChild).key(this.widget().secondChild.key())
            );
        });
    }
}
