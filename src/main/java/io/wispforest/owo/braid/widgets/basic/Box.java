package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.framework.instance.OptionalChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.OptionalChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class Box extends OptionalChildInstanceWidget {

    public final Color color;
    public final boolean outline;

    public Box(Color color, boolean outline, @Nullable Widget child) {
        super(child);
        this.color = color;
        this.outline = outline;
    }

    public Box(Color color, @Nullable Widget child) {
        this(color, false, child);
    }

    public Box(Color color, boolean outline) {
        this(color, outline, null);
    }

    public Box(Color color) {
        this(color, false);
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
        public void draw(BraidGraphics graphics) {
            if (this.widget.outline) {
                graphics.drawRectOutline(0, 0, (int) this.transform.width(), (int) this.transform.height(), this.widget.color.argb());
            } else {
                graphics.fill(0, 0, (int) this.transform.width(), (int) this.transform.height(), this.widget.color.argb());
            }

            super.draw(graphics);
        }
    }
}
