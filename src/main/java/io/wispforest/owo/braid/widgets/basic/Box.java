package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.OptionalChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.OptionalChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import org.jetbrains.annotations.Nullable;

public class Box extends OptionalChildInstanceWidget {

    public final Color color;

    public Box(Color color, @Nullable Widget child) {
        super(child);
        this.color = color;
    }

    public Box(Color color) {
        this(color, null);
    }

    @Override
    public OptionalChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends OptionalChildWidgetInstance.ShrinkWrap<Box> {

        public Instance(Box widget) {
            super(widget);
        }

        @Override
        public void draw(OwoUIDrawContext ctx) {
            ctx.fill(0, 0, (int) this.transform.width(), (int) this.transform.height(), this.widget.color.argb());
            super.draw(ctx);
        }
    }
}
