package io.wispforest.owo.braid.widgets.flex;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/// A [Flex], restricted to the vertical axis.
///
/// See the [Flex] documentation for details
public class Column extends Flex {
    public Column(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        @Nullable Widget separator,
        List<? extends Widget> children
    ) {
        super(LayoutAxis.VERTICAL, mainAxisAlignment, crossAxisAlignment, separator, children);
    }

    /// Create a column without a separator
    public Column(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        List<? extends Widget> children
    ) {
        this(mainAxisAlignment, crossAxisAlignment, null, children);
    }

    /// Create a column without a separator
    public Column(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        Widget... children
    ) {
        this(mainAxisAlignment, crossAxisAlignment, null, Arrays.asList(children));
    }

    /// Create a column with default (start) alignment
    /// on both axes
    public Column(
        @Nullable Widget separator,
        List<? extends Widget> children
    ) {
        this(MainAxisAlignment.START, CrossAxisAlignment.START, separator, children);
    }

    /// Create a column with default (start) alignment
    /// on both axes and no separator
    public Column(
        List<? extends Widget> children
    ) {
        this(MainAxisAlignment.START, CrossAxisAlignment.START, null, children);
    }

    /// Create a column with default (start) alignment
    /// on both axes and no separator
    public Column(
        Widget... children
    ) {
        this(Arrays.asList(children));
    }
}
