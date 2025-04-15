package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.util.ScissorStack;

public class Clip extends SingleChildInstanceWidget {
    public Clip(Widget child) {
        super(child);
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance<Clip> {

        public Instance(Clip widget) {
            super(widget);
        }

        @Override
        public void draw(OwoUIDrawContext ctx) {
            ScissorStack.push(0, 0, (int) this.transform.width(), (int) this.transform.height(), ctx);
            super.draw(ctx);
            ctx.draw();
            ScissorStack.pop();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.sizeToChild(constraints, this.child);
        }
    }
}
