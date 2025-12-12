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

import java.util.ArrayList;
import java.util.List;


public class MultiSplitPane extends StatefulWidget {

    public final LayoutAxis mainAxis;
    public final MainAxisAlignment mainAxisAlignment;
    public final CrossAxisAlignment crossAxisAlignment;
    public final List<? extends Widget> children;

    public MultiSplitPane(
        LayoutAxis mainAxis,
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        List<? extends Widget> children
    ) {
        this.mainAxis = mainAxis;
        this.mainAxisAlignment = mainAxisAlignment;
        this.crossAxisAlignment = crossAxisAlignment;
        this.children = children;
    }

    @Override
    public WidgetState<MultiSplitPane> createState() {
        return new MultiSplitPaneState();
    }
}

class MultiSplitPaneState extends WidgetState<MultiSplitPane> {

    private List<Double> splits = null;

    @Override
    public Widget build(BuildContext context) {
        return new LayoutBuilder((innerContext, constraints) -> {
            var axis = this.widget().mainAxis;
            var children = this.widget().children;
            var maxSize = constraints.maxOnAxis(axis) - ((children.size() - 2) * 2);

            var splitSize = maxSize / (children.size());

            if (this.splits == null) {
                this.splits = new ArrayList<>();
                for (int i = 0; i < children.size() - 1; i++) this.splits.add(splitSize * (i + 1));
            }

            var widgets = new ArrayList<Widget>();

            for (int i = 0; i < children.size(); i++) {
                var child = children.get(i);

//                var split = MathHelper.clamp(this.splits.get(i), .1, .9) * maxSize;

                var min = i == 0 ? 0 : this.splits.get(i - 1);
                var max = i == children.size() - 1 ? maxSize : this.splits.get(i);

                var childConstraints = Constraints.tight(axis.createSize(max - min, constraints.maxOnAxis(axis.opposite())));
                widgets.add(new Constrain(childConstraints, child).key(child.key()));

                if (i < children.size() - 1) {
                    int finalI = i;
                    widgets.add(new Flexible(
                        new MouseArea(
                            widget -> widget
                                .dragCallback((x, y, dx, dy) -> setState(() -> {
                                    this.splits.set(finalI,  this.splits.get(finalI) + axis.choose(dx, dy));
                                }))
                                .dragEndCallback(() -> {
                                    this.splits.set(finalI, Mth.clamp(this.splits.get(finalI), .1 * maxSize, .9 * maxSize));
                                })
                                .cursorStyleSupplier((x, y) -> axis.choose(CursorStyle.HORIZONTAL_RESIZE, CursorStyle.VERTICAL_RESIZE)),
                            new Box(Color.WHITE)
                        )
                    ).key(Key.of("splitter-" + i)));
                }
            }

            return new Flex(
                axis,
                this.widget().mainAxisAlignment,
                this.widget().crossAxisAlignment,
                null,
                widgets
            );


//            var split = Math.floor(MathHelper.clamp(this.splitCoordinate, .1 * maxSize, .9 * maxSize));
//
//            var firstConstraints = Constraints.tight(axis.createSize(split, constraints.maxOnAxis(axis.opposite())));
//            var secondConstraints = Constraints.tight(axis.createSize(maxSize - split, constraints.maxOnAxis(axis.opposite())));
//
//            return new Flex(
//                axis,
//                MainAxisAlignment.START,
//                CrossAxisAlignment.START,
//                new Constrain(firstConstraints, this.widget().firstChild).key(this.widget().firstChild.key()),
//                new Flexible(
//                    new MouseArea(
//                        widget -> widget
//                            .dragCallback((x, y, dx, dy) -> setState(() -> {
//                                this.splitCoordinate = this.splitCoordinate + axis.choose(dx, dy);
//                            }))
//                            .dragEndCallback(() -> {
//                                this.splitCoordinate = MathHelper.clamp(this.splitCoordinate, .1 * maxSize, .9 * maxSize);
//                            })
//                            .cursorStyleSupplier((x, y) -> axis.choose(CursorStyle.HORIZONTAL_RESIZE, CursorStyle.VERTICAL_RESIZE)),
//                        new Box(Color.WHITE)
//                    )
//                ).key(Key.of("splitter")),
//                new Constrain(secondConstraints, this.widget().secondChild).key(this.widget().secondChild.key())
//            );
        });
    }
}
