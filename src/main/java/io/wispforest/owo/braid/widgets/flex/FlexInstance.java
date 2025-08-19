package io.wispforest.owo.braid.widgets.flex;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import io.wispforest.owo.braid.core.BraidUtils;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.MultiChildWidgetInstance;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;

import java.util.OptionalDouble;
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
                this.widget.crossAxisAlignment.computeChildOffset(
                    size.getExtent(crossAxis) - child.transform.getExtent(crossAxis)
                )
            );

            mainAxisOffset += child.transform.getExtent(mainAxis) + betweenSpace;
        }
    }

    @Override
    protected double measureIntrinsicWidth(double height) {
        return this.widget.mainAxis == LayoutAxis.HORIZONTAL ? this.measureMainAxis(height) : this.measureCrossAxis(height);
    }

    @Override
    protected double measureIntrinsicHeight(double width) {
        return this.widget.mainAxis == LayoutAxis.VERTICAL ? this.measureMainAxis(width) : this.measureCrossAxis(width);
    }

    @Override
    protected OptionalDouble measureBaselineOffset() {
        return switch (this.widget.mainAxis) {
            case VERTICAL -> this.computeFirstBaselineOffset();
            case HORIZONTAL -> this.computeHighestBaselineOffset();
        };
    }

    @SuppressWarnings("DataFlowIssue")
    private double measureMainAxis(double crossExtent) {
        var horizontal = this.widget.mainAxis == LayoutAxis.HORIZONTAL;
        var nonFlexSize = this.children.stream()
            .filter(element -> !(element.parentData instanceof FlexParentData))
            .mapToDouble(e -> horizontal ? e.getIntrinsicWidth(crossExtent) : e.getIntrinsicHeight(crossExtent))
            .sum();

        var totalFlexFactor = 0.0;

        WidgetInstance<?> largestFlexChild = null;
        double largestFlexChildSize = 0;
        double largestFlexChildFlexFactor = 0;

        for (var flexChild : Iterables.filter(this.children, element -> element.parentData instanceof FlexParentData)) {
            totalFlexFactor += ((FlexParentData) flexChild.parentData).flexFactor;

            var size = horizontal ? flexChild.getIntrinsicWidth(crossExtent) : flexChild.getIntrinsicHeight(crossExtent);
            if (size > largestFlexChildSize) {
                largestFlexChild = flexChild;
                largestFlexChildSize = size;
                largestFlexChildFlexFactor = ((FlexParentData) flexChild.parentData).flexFactor;
            }
        }

        var flexSize = largestFlexChild != null ? (totalFlexFactor / largestFlexChildFlexFactor) * largestFlexChildSize : 0;

        return nonFlexSize + flexSize;
    }

    @SuppressWarnings("DataFlowIssue")
    private double measureCrossAxis(double mainExtent) {
        var horizontal = this.widget.mainAxis == LayoutAxis.HORIZONTAL;

        var crossSize = 0.0;

        var nonFlexSize = 0.0;
        for (var child : Iterables.filter(this.children, element -> !(element.parentData instanceof FlexParentData))) {
            var childSize = horizontal ? child.getIntrinsicHeight(mainExtent) : child.getIntrinsicWidth(mainExtent);

            nonFlexSize += childSize;
            crossSize = Math.max(crossSize, childSize);
        }

        var flexChildren = Iterables.filter(children, (element) -> element.parentData instanceof FlexParentData);
        var totalFlexFactor = Streams.stream(flexChildren).mapToDouble(e -> ((FlexParentData) e.parentData).flexFactor).sum();

        for (var child : flexChildren) {
            var childSpace = (mainExtent - nonFlexSize) * (totalFlexFactor / ((FlexParentData) child.parentData).flexFactor);

            crossSize = Math.max(
                crossSize,
                horizontal ? child.getIntrinsicHeight(childSpace) : child.getIntrinsicWidth(childSpace)
            );
        }

        return crossSize;
    }
}
