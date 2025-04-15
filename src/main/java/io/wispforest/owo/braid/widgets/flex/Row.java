package io.wispforest.owo.braid.widgets.flex;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.Arrays;
import java.util.List;

public class Row extends Flex {
    public Row(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        List<Widget> children
    ) {
        super(LayoutAxis.HORIZONTAL, mainAxisAlignment, crossAxisAlignment, children);
    }

    public Row(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        Widget... children
    ) {
        this(mainAxisAlignment, crossAxisAlignment, Arrays.asList(children));
    }

    public Row(
        List<Widget> children
    ) {
        this(MainAxisAlignment.START, CrossAxisAlignment.START, children);
    }

    public Row(
        Widget... children
    ) {
        this(Arrays.asList(children));
    }
}
