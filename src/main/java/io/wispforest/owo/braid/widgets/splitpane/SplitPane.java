package io.wispforest.owo.braid.widgets.splitpane;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class SplitPane extends StatelessWidget {
    public final LayoutAxis axis;
    public final @Nullable SplitController controller;
    public final @Nullable SplitPaneStyle style;
    public final boolean enabled;
    public final List<? extends Widget> children;

    public SplitPane(
        LayoutAxis axis,
        @Nullable SplitController controller,
        @Nullable SplitPaneStyle style,
        boolean enabled,
        List<? extends Widget> children
    ) {
        this.axis = axis;
        this.controller = controller;
        this.style = style;
        this.enabled = enabled;
        this.children = children;
    }

    public SplitPane(LayoutAxis axis, Widget... children) {
        this(axis, null, null, true, Arrays.asList(children));
    }

    public SplitPane(LayoutAxis axis, SplitController controller, Widget... children) {
        this(axis, controller, null, true, Arrays.asList(children));
    }

    public SplitPane(LayoutAxis axis, SplitPaneStyle style, Widget... children) {
        this(axis, null, style, true, Arrays.asList(children));
    }

    public SplitPane(LayoutAxis axis, SplitController controller, SplitPaneStyle style, Widget... children) {
        this(axis, controller, style, true, Arrays.asList(children));
    }

    @Override
    public Widget build(BuildContext context) {
        return new RawSplitPane(this.axis, this.controller, this.style, this.enabled, this.children);
    }
}
