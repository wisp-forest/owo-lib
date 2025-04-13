package io.wispforest.owo.braid.framework.instance;

import com.google.common.base.Preconditions;
import io.wispforest.owo.braid.framework.widget.InstanceWidget;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;

public abstract class SingleChildWidgetInstance<T extends InstanceWidget> extends WidgetInstance<T> {

    protected WidgetInstance<?> child;

    public SingleChildWidgetInstance(T widget) {
        super(widget);
    }

    @Override
    public void draw(DrawContext ctx) {
        this.drawChild(ctx, this.child);
    }

    @Override
    public void visitChildren(Visitor visitor) {
        visitor.visit(this.child);
    }

    public WidgetInstance<?> child() {
        Preconditions.checkNotNull(this.child, "tried to retrieve child of SingleChildWidgetInstance before it was set");
        return this.child;
    }

    public void setChild(WidgetInstance<?> value) {
        if (value == this.child) return;

        this.child = this.adopt(value);
        this.markNeedsLayout();
    }
}
