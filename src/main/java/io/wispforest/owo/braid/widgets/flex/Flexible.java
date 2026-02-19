package io.wispforest.owo.braid.widgets.flex;

import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.VisitorWidget;

/// A widget which is forced to fill the remaining space in its
/// [Flex] parent. The space is weighted between all Flexible
/// children according to their [#flexFactor]
///
/// Check the [Flex] documentation for details
///
/// **Note:** For Flexible to work, it is essential that the path from it to
/// its enclosing [Flex] contains only stateful and stateless widgets.
public class Flexible extends VisitorWidget {

    /// The relative proportion of the parent's
    /// remaining space to assign to this widget
    public final double flexFactor;

    public Flexible(double flexFactor, Widget child) {
        super(child);
        this.flexFactor = flexFactor;
    }

    /// Create a Flexible with the default flex factor 1
    public Flexible(Widget child) {
        this(1, child);
    }

    private static final Visitor<Flexible> VISITOR = (widget, instance) -> {
        if (instance.parentData instanceof FlexParentData data) {
            data.flexFactor = widget.flexFactor;
        } else {
            instance.parentData = new FlexParentData(widget.flexFactor);
        }

        instance.markNeedsLayout();
    };

    @Override
    public Proxy<?> proxy() {
        return new VisitorWidget.Proxy<>(this, VISITOR);
    }
}
