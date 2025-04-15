package io.wispforest.owo.braid.widgets.flex;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.instance.MultiChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.MultiChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.Arrays;
import java.util.List;

public class Flex extends MultiChildInstanceWidget {

    public final LayoutAxis mainAxis;
    public final MainAxisAlignment mainAxisAlignment;
    public final CrossAxisAlignment crossAxisAlignment;

    public Flex(
        LayoutAxis mainAxis,
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        List<Widget> children
    ) {
        super(children);
        this.mainAxis = mainAxis;
        this.mainAxisAlignment = mainAxisAlignment;
        this.crossAxisAlignment = crossAxisAlignment;
    }

    public Flex(
        LayoutAxis mainAxis,
        MainAxisAlignment mainAxisAlignment,
        CrossAxisAlignment crossAxisAlignment,
        Widget... children
    ) {
        this(mainAxis, mainAxisAlignment, crossAxisAlignment, Arrays.asList(children));
    }

    @Override
    public MultiChildWidgetInstance<?> instantiate() {
        return new FlexInstance(this);
    }
}
