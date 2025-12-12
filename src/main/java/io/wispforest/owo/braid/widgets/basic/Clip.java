package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.framework.instance.HitTestState;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.client.gui.navigation.ScreenRectangle;

// TODO: stencil clip
//  also warn in docs about transforms which aren't pure translations
public class Clip extends SingleChildInstanceWidget {

    public final boolean clipHitTest;
    public final boolean clipDrawing;

    public Clip(boolean clipHitTest, boolean clipDrawing, Widget child) {
        super(child);
        this.clipHitTest = clipHitTest;
        this.clipDrawing = clipDrawing;
    }

    public Clip(Widget child) {
        this(true, true, child);
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<Clip> {

        public Instance(Clip widget) {
            super(widget);
        }

        @Override
        public void draw(BraidGraphics graphics) {
            if (!this.widget.clipDrawing) {
                super.draw(graphics);
                return;
            }

            graphics.scissorStack.push(new ScreenRectangle(0, 0, (int) this.transform.width(), (int) this.transform.height()).transformMaxBounds(graphics.pose()));
            super.draw(graphics);
            graphics.disableScissor();
        }

        @Override
        public void hitTest(double x, double y, HitTestState state) {
            if (this.widget.clipHitTest && (x < 0 || x > this.transform.width() || y < 0 || y > this.transform.height())) {
                return;
            }

            super.hitTest(x, y, state);
        }
    }
}
