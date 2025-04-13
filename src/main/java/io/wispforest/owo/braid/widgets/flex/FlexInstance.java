package io.wispforest.owo.braid.widgets.flex;

import com.google.common.collect.Iterables;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.MultiChildWidgetInstance;
import io.wispforest.owo.braid.framework.instance.WidgetTransform;

import java.util.function.BiFunction;
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
                ? maxOnAxis(constraints, crossAxis)
                : minOnAxis(constraints, crossAxis);

        var childConstraints = Constraints.of(
            mainAxis == LayoutAxis.VERTICAL ? crossAxisMinimum : 0,
            mainAxis == LayoutAxis.HORIZONTAL ? crossAxisMinimum : 0,
            mainAxis == LayoutAxis.VERTICAL ? maxOnAxis(constraints, crossAxis) : Double.POSITIVE_INFINITY,
            mainAxis == LayoutAxis.HORIZONTAL ? maxOnAxis(constraints, crossAxis) : Double.POSITIVE_INFINITY
        );

        // first, lay out all non-flex children and store their sizes
        var childSizes = this.children.stream()
            .filter(element -> !(element.parentData instanceof FlexParentData))
            .map((e) -> e.layout(childConstraints))
            .collect(Collectors.toList());

        // now, compute the remaining space on the main axis
        var remainingSpace = Math.max(
            maxOnAxis(constraints, mainAxis) - fold(childSizes, 0.0, (acc, size) -> acc + getSizeExtent(size, mainAxis)),
            0
        );

        // get the flex children and compute the total flex factor in order
        // to divvy up the remaining space properly later
        var flexChildren = Iterables.filter(children, (element) -> element.parentData instanceof FlexParentData);
        var totalFlexFactor = fold(
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
        var size = fold(
            childSizes,
            Size.zero(),
            (acc, elem) -> mainAxis.createSize(
                getSizeExtent(acc, mainAxis) + getSizeExtent(elem, mainAxis),
                Math.max(getSizeExtent(acc, crossAxis), getSizeExtent(elem, crossAxis))
            )
        ).constrained(constraints);

        this.transform.setSize(size);

        // distribute remaining space on the main axis
        var freeSpace = getSizeExtent(size, mainAxis) - fold(childSizes, 0.0, (acc, elem) -> acc + getSizeExtent(elem, mainAxis));

        var leadingSpace = this.widget.mainAxisAlignment.leadingSpace(freeSpace, childSizes.size());
        var betweenSpace = this.widget.mainAxisAlignment.between(freeSpace, childSizes.size());

        // move children into position and apply cross-axis alignment
        var mainAxisOffset = leadingSpace;
        for (var child : children) {
            setTransformCoordinate(child.transform, mainAxis, mainAxisOffset);

            setTransformCoordinate(
                child.transform,
                crossAxis,
                this.widget.crossAxisAlignment._computeChildOffset(
                    getSizeExtent(size, crossAxis) - getTransformExtent(child.transform, crossAxis)
                )
            );

            mainAxisOffset += getTransformExtent(child.transform, mainAxis) + betweenSpace;
        }
    }

    protected static double minOnAxis(Constraints constraints, LayoutAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> constraints.minWidth();
            case VERTICAL -> constraints.minHeight();
        };
    }

    protected static double maxOnAxis(Constraints constraints, LayoutAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> constraints.maxWidth();
            case VERTICAL -> constraints.maxHeight();
        };
    }

    protected static double getSizeExtent(Size size, LayoutAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> size.width();
            case VERTICAL -> size.height();
        };
    }

    protected static double getTransformExtent(WidgetTransform transform, LayoutAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> transform.width();
            case VERTICAL -> transform.height();
        };
    }

    protected static void setTransformExtent(WidgetTransform transform, LayoutAxis axis, double value) {
        switch (axis) {
            case HORIZONTAL -> transform.setWidth(value);
            case VERTICAL -> transform.setHeight(value);
        }
        ;
    }

    protected static double getTransformCoordinate(WidgetTransform transform, LayoutAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> transform.x();
            case VERTICAL -> transform.y();
        };
    }

    protected static void setTransformCoordinate(WidgetTransform transform, LayoutAxis axis, double value) {
        switch (axis) {
            case HORIZONTAL -> transform.setX(value);
            case VERTICAL -> transform.setY(value);
        }
    }

    protected static <S, T> T fold(Iterable<S> values, T initial, BiFunction<T, S, T> step) {
        var result = initial;
        for (var value : values) {
            result = step.apply(result, value);
        }

        return result;
    }
}
