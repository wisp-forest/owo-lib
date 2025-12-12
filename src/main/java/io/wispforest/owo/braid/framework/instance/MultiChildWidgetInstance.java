package io.wispforest.owo.braid.framework.instance;

import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.core.BraidUtils;
import io.wispforest.owo.braid.framework.widget.MultiChildInstanceWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

public abstract class MultiChildWidgetInstance<T extends MultiChildInstanceWidget> extends WidgetInstance<T> {

    public List<WidgetInstance<?>> children = new ArrayList<>();

    public MultiChildWidgetInstance(T widget) {
        super(widget);
    }

    @Override
    public void draw(BraidGraphics graphics) {
        for (var child : this.children) {
            this.drawChild(graphics, child);
        }
    }

    @Override
    public void visitChildren(Visitor visitor) {
        for (var child : this.children) {
            visitor.visit(child);
        }
    }

    public void insertChild(int index, WidgetInstance<?> child) {
        this.children.set(index, this.adopt(child));
        this.markNeedsLayout();
    }

    // ---

    protected OptionalDouble computeFirstBaselineOffset() {
        for (var child : this.children) {
            var childBaseline = child.getBaselineOffset();
            if (childBaseline.isEmpty()) continue;

            return OptionalDouble.of(childBaseline.getAsDouble() + child.transform.y);
        }

        return OptionalDouble.empty();
    }

    protected OptionalDouble computeHighestBaselineOffset() {
        return BraidUtils.fold(this.children, null, (acc, child) -> {
            var childBaseline = child.getBaselineOffset();
            if (childBaseline.isEmpty()) return acc;

            return baselineMin(acc, OptionalDouble.of(childBaseline.getAsDouble() + child.transform.y));
        });
    }

    private static OptionalDouble baselineMin(OptionalDouble a, OptionalDouble b) {
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;
        return a.getAsDouble() <= b.getAsDouble() ? a : b;
    }
}
