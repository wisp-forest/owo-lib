package io.wispforest.owo.braid.widgets.flex;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class Column extends Flex {
    public Column(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        @Nullable Widget separator,
        List<? extends Widget> children
    ) {
        super(LayoutAxis.VERTICAL, mainAxisAlignment, crossAxisAlignment, separator, children);
    }

    public Column(
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        Widget... children
    ) {
        this(mainAxisAlignment, crossAxisAlignment, null, Arrays.asList(children));
    }

    public Column(
        @Nullable Widget separator,
        List<? extends Widget> children
    ) {
        this(MainAxisAlignment.START, CrossAxisAlignment.START, separator, children);
    }

    public Column(
        List<? extends Widget> children
    ) {
        this(MainAxisAlignment.START, CrossAxisAlignment.START, null, children);
    }

    public Column(
        Widget... children
    ) {
        this(Arrays.asList(children));
    }
}
