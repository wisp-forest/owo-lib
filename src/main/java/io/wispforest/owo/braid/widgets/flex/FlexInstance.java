package io.wispforest.owo.braid.widgets.flex;

import com.google.common.collect.Iterables;
import io.wispforest.owo.braid.core.BraidUtils;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.MultiChildWidgetInstance;

import java.util.stream.Collectors;

public class FlexInstance extends MultiChildWidgetInstance<Flex> {

    public FlexInstance(Flex widget) {
        super(widget);
    }

    @Override
    public void setWidget(Flex widget) {
        if (this.widget.mainAxis == widget.mainAxis
            && this.widget.mainAxisAlignment == widget.mainAxisAlignment
            && this.widget.crossAxisAlignment == widget.crossAxisAlignment) {
            return;
        }

        super.setWidget(widget);
        this.markNeedsLayout();
    }

    @Override
    protected void doLayout(Constraints constraints) {
        var mainAxis = widget.mainAxis;
        var crossAxis = mainAxis.opposite();

        var crossAxisMinimum =
            widget.crossAxisAlignment == CrossAxisAlignment.STRETCH
                ? constraints.maxOnAxis(crossAxis)
                : constraints.minOnAxis(crossAxis);

        var childConstraints = Constraints.of(
            mainAxis == LayoutAxis.VERTICAL ? crossAxisMinimum : 0,
            mainAxis == LayoutAxis.HORIZONTAL ? crossAxisMinimum : 0,
            mainAxis == LayoutAxis.VERTICAL ? constraints.maxOnAxis(crossAxis) : Double.POSITIVE_INFINITY,
            mainAxis == LayoutAxis.HORIZONTAL ? constraints.maxOnAxis(crossAxis) : Double.POSITIVE_INFINITY
        );

        // first, lay out all non-flex children and store their sizes
        var childSizes = this.children.stream()
            .filter(element -> !(element.parentData instanceof FlexParentData))
            .map((e) -> e.layout(childConstraints))
            .collect(Collectors.toList());

        // now, compute the remaining space on the main axis
        var remainingSpace = Math.max(
            constraints.maxOnAxis(mainAxis) - BraidUtils.fold(childSizes, 0.0, (acc, size) -> acc + size.getExtent(mainAxis)),
            0
        );

        // get the flex children and compute the total flex factor in order
        // to divvy up the remaining space properly later
        var flexChildren = Iterables.filter(children, (element) -> element.parentData instanceof FlexParentData);
        var totalFlexFactor = BraidUtils.fold(
            flexChildren,
            0.0,
            (previousValue, element) -> previousValue + ((FlexParentData) element.parentData).flexFactor
        );

        // lay out all flex children with (for now) tight constraints
        // on the main axis according to their allotted space
        for (var child : flexChildren) {
            var space = remainingSpace * (((FlexParentData) child.parentData).flexFactor / totalFlexFactor);
            childSizes.add(
                child.layout(
                    childConstraints.respecting(
                        Constraints.tightOnAxis(
                            mainAxis == LayoutAxis.HORIZONTAL ? space : null,
                            mainAxis == LayoutAxis.VERTICAL ? space : null
                        )
                    )
                )
            );
        }

        // compute and apply the final size of ourselves
        var size = BraidUtils.fold(
            childSizes,
            Size.zero(),
            (acc, elem) -> mainAxis.createSize(
                acc.getExtent(mainAxis) + elem.getExtent(mainAxis),
                Math.max(acc.getExtent(crossAxis), elem.getExtent(crossAxis))
            )
        ).constrained(constraints);

        this.transform.setSize(size);

        // distribute remaining space on the main axis
        var freeSpace = size.getExtent(mainAxis) - BraidUtils.fold(childSizes, 0.0, (acc, elem) -> acc + elem.getExtent(mainAxis));

        var leadingSpace = this.widget.mainAxisAlignment.leadingSpace(freeSpace, childSizes.size());
        var betweenSpace = this.widget.mainAxisAlignment.between(freeSpace, childSizes.size());

        // move children into position and apply cross-axis alignment
        var mainAxisOffset = leadingSpace;
        for (var child : children) {
            child.transform.setCoordinate(mainAxis, mainAxisOffset);

            child.transform.setCoordinate(
                crossAxis,
                this.widget.crossAxisAlignment._computeChildOffset(
                    size.getExtent(crossAxis) - child.transform.getExtent(crossAxis)
                )
            );

            mainAxisOffset += child.transform.getExtent(mainAxis) + betweenSpace;
        }
    }
}
