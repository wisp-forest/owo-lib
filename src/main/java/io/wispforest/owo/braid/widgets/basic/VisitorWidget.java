package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.ComposedProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public abstract class VisitorWidget extends Widget {
    public final Widget child;

    protected VisitorWidget(Widget child) {
        this.child = child;
    }

    @Override
    public abstract Proxy<?> proxy();

    public static class Proxy<T extends VisitorWidget> extends ComposedProxy {

        public final VisitorWidget.Visitor<T> visitor;
        public WidgetInstance<?> descendantInstance;

        public Proxy(Widget widget, VisitorWidget.Visitor<T> visitor) {
            super(widget);
            this.visitor = visitor;
        }

        @Override
        public void mount(WidgetProxy parent, @Nullable Object slot) {
            super.mount(parent, slot);
            this.rebuild();
        }

        @Override
        public void updateWidget(Widget newWidget) {
            super.updateWidget(newWidget);
            this.rebuild(true);
        }

        @Override
        protected void doRebuild() {
            super.doRebuild();
            this.child = this.refreshChild(this.child, ((VisitorWidget)this.widget()).child, this.slot());

            if (this.descendantInstance != null) {
                this.visitor.visit((T) this.widget(), this.descendantInstance);
            }
        }

        @Override
        public void notifyDescendantInstance(@Nullable WidgetInstance<?> instance, @Nullable Object slot) {
            this.visitor.visit((T) this.widget(), instance);
            this.descendantInstance = instance;
        }
    }

    @FunctionalInterface
    public interface Visitor<T> {
        void visit(T widget, WidgetInstance<?> instance);
    }
}
