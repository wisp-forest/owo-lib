package io.wispforest.owo.braid.widgets.flex;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.Arrays;
import java.util.List;

public class Column extends Flex {
    public Column(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        List<Widget> children
    ) {
        super(LayoutAxis.VERTICAL, mainAxisAlignment, crossAxisAlignment, children);
    }

    public Column(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        Widget... children
    ) {
        this(mainAxisAlignment, crossAxisAlignment, Arrays.asList(children));
    }

    public Column(
        List<Widget> children
    ) {
        this(MainAxisAlignment.START, CrossAxisAlignment.START, children);
    }

    public Column(
        Widget... children
    ) {
        this(Arrays.asList(children));
    }
}

