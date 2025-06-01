package io.wispforest.owo.braid.widgets.flex;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.instance.MultiChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.MultiChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
        @Nullable Widget separator,
        List<? extends Widget> children
    ) {
        super(Util.make(() -> {
            if (separator == null || children.size() < 2) return children;

            var result = new ArrayList<Widget>();
            for (var i = 0; i < children.size() - 1; i++) {
                result.add(children.get(i));
                result.add(separator);
            }

            result.add(children.getLast());
            return result;
        }));
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
        this(mainAxis, mainAxisAlignment, crossAxisAlignment, null, Arrays.asList(children));
    }

    @Override
    public MultiChildWidgetInstance<?> instantiate() {
        return new FlexInstance(this);
    }
}
