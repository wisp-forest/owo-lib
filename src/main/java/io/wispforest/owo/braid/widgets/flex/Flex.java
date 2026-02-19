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

/// A Flex places its children one after another in a horizontal
/// or vertical list, as dictated by its [#mainAxis].
///
/// Child alignment on both axes is given by [#mainAxisAlignment]
/// and [#crossAxisAlignment] respectively.
///
/// Children receive loose and unbounded constraints on the main axis
/// and the Flex's cross axis constraints are passed down unchanged. If
/// cross axis alignment is set to [CrossAxisAlignment#STRETCH], cross
/// axis constraints are tightened to their maximum.
///
/// Children can be made to receive tight main axis constraints by wrapping
/// them in a [Flexible]. After all non-flexible children are laid out,
/// the remaining main axis space is divided up amongst all [Flexible] children,
/// weighted by their respective [Flexible#flexFactor].
///
/// A Flex can have an optional `separator`, which is inserted between each child
public class Flex extends MultiChildInstanceWidget {

    /// The axis along which the children of this widget
    /// are placed
    public final LayoutAxis mainAxis;

    /// How remaining space on the main axis, if any,
    /// should be distributed between and around the children
    public final MainAxisAlignment mainAxisAlignment;

    /// How remaining space on the cross axis, if any,
    /// should be distributed around the children
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

    /// Create a flex without a separator
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
