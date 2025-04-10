package io.wispforest.owo.braid.framework.instance;

import io.wispforest.owo.braid.framework.widget.MultiChildInstanceWidget;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiChildWidgetInstance<T extends MultiChildInstanceWidget> extends WidgetInstance<T> {

    public List<WidgetInstance<?>> children = new ArrayList<>();

    public MultiChildWidgetInstance(T widget) {
        super(widget);
    }

    @Override
    public void draw(DrawContext ctx) {
        for (var child : this.children) {
            this.drawChild(ctx, child);
        }
    }

    @Override
    public void visitChildren(Visitor visitor) {
        for (var child : this.children) {
            visitor.visit(child);
        }
    }

    public void insertChild(int index, WidgetInstance<?> child) {
        this.children.set(index, child);
        this.markNeedsLayout();
    }
}
