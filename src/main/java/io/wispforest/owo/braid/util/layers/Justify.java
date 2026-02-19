package io.wispforest.owo.braid.util.layers;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

public class Justify extends SingleChildInstanceWidget {

    public final double x;
    public final double y;

    public Justify(double x, double y, Widget child) {
        super(child);
        this.x = x;
        this.y = y;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<Justify> {

        public Instance(Justify widget) {
            super(widget);
        }

        @Override
        public void setWidget(Justify widget) {
            super.setWidget(widget);
            this.justify();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            super.doLayout(constraints);
            this.justify();
        }

        private void justify() {
            this.child.transform.setX(-this.child.transform.width() * this.widget.x);
            this.child.transform.setY(-this.child.transform.height() * this.widget.y);
        }
    }
}
