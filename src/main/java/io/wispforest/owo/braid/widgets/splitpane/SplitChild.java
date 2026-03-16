package io.wispforest.owo.braid.widgets.splitpane;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import org.jetbrains.annotations.Nullable;

public class SplitChild extends StatelessWidget {

    public final Widget child;
    protected @Nullable Double size = null;
    protected double weight = 1;
    protected double minSize = 0;
    protected double maxSize = Double.POSITIVE_INFINITY;

    public SplitChild(@Nullable WidgetSetupCallback<SplitChild> setupCallback, Widget child) {
        this.child = child;
        if (setupCallback != null) setupCallback.setup(this);
    }

    public SplitChild(Widget child) {
        this(null, child);
    }

    public SplitChild size(double size) {
        this.assertMutable();
        this.size = size;
        return this;
    }

    public SplitChild weight(double weight) {
        this.assertMutable();
        this.weight = weight;
        return this;
    }

    public SplitChild minSize(double minSize) {
        this.assertMutable();
        this.minSize = minSize;
        return this;
    }

    public SplitChild maxSize(double maxSize) {
        this.assertMutable();
        this.maxSize = maxSize;
        return this;
    }

    public SplitChild clampSize(double min, double max) {
        this.assertMutable();
        this.minSize = min;
        this.maxSize = max;
        return this;
    }

    @Override
    public Widget build(BuildContext context) {
        return this.child;
    }
}
