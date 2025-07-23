package io.wispforest.owo.braid.widgets.flex;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class Row extends Flex {
    public Row(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        @Nullable Widget separator,
        List<? extends Widget> children
    ) {
        super(LayoutAxis.HORIZONTAL, mainAxisAlignment, crossAxisAlignment, separator, children);
    }

    public Row(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        List<? extends Widget> children
    ) {
        this(mainAxisAlignment, crossAxisAlignment, null, children);
    }

    public Row(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        Widget... children
    ) {
        this(mainAxisAlignment, crossAxisAlignment, null, Arrays.asList(children));
    }

    public Row(
        @Nullable Widget separator,
        List<? extends Widget> children
    ) {
        this(MainAxisAlignment.START, CrossAxisAlignment.START, separator, children);
    }

    public Row(
        List<? extends Widget> children
    ) {
        this(MainAxisAlignment.START, CrossAxisAlignment.START, null, children);
    }

    public Row(
        Widget... children
    ) {
        this(Arrays.asList(children));
    }
}
