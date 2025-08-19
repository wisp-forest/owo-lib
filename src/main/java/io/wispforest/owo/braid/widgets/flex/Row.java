package io.wispforest.owo.braid.widgets.flex;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/// A [Flex], restricted to the horizontal axis.
///
/// See the [Flex] documentation for details
public class Row extends Flex {
    public Row(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        @Nullable Widget separator,
        List<? extends Widget> children
    ) {
        super(LayoutAxis.HORIZONTAL, mainAxisAlignment, crossAxisAlignment, separator, children);
    }

    /// Create a row without a separator
    public Row(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        List<? extends Widget> children
    ) {
        this(mainAxisAlignment, crossAxisAlignment, null, children);
    }

    /// Create a row without a separator
    public Row(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        Widget... children
    ) {
        this(mainAxisAlignment, crossAxisAlignment, null, Arrays.asList(children));
    }

    /// Create a row with default (start) alignment
    /// on both axes
    public Row(
        @Nullable Widget separator,
        List<? extends Widget> children
    ) {
        this(MainAxisAlignment.START, CrossAxisAlignment.START, separator, children);
    }

    /// Create a column with default (start) alignment
    /// on both axes and no separator
    public Row(
        List<? extends Widget> children
    ) {
        this(MainAxisAlignment.START, CrossAxisAlignment.START, null, children);
    }

    /// Create a column with default (start) alignment
    /// on both axes and no separator
    public Row(
        Widget... children
    ) {
        this(Arrays.asList(children));
    }
}
