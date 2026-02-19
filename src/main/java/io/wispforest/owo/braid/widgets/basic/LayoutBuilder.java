package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.OptionalChildWidgetInstance;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.BuildScope;
import io.wispforest.owo.braid.framework.proxy.InstanceWidgetProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import io.wispforest.owo.braid.framework.widget.InstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class LayoutBuilder extends InstanceWidget {

    public final Callback builder;

    public LayoutBuilder(Callback builder) {
        this.builder = builder;
    }

    @Override
    public WidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    @Override
    public WidgetProxy proxy() {
        return new Proxy(this);
    }

    public static class Proxy extends InstanceWidgetProxy {

        protected final BuildScope scope = new BuildScope(() -> {
            this.instance.markNeedsLayout();
        });
        protected WidgetProxy child;

        protected Proxy(InstanceWidget widget) {
            super(widget);
            this.instance().callback = this::rebuild;
        }

        @Override
        public Instance instance() {
            return (Instance) super.instance();
        }

        @Override
        public BuildScope buildScope() {
            return this.scope;
        }

        @Override
        public void updateWidget(Widget newWidget) {
            super.updateWidget(newWidget);
            this.instance.markNeedsLayout();
        }

        protected void rebuild(Constraints constraints) {
            var newWidget = ((LayoutBuilder) this.widget()).builder.build(this, constraints);
            this.child = this.refreshChild(this.child, newWidget, null);

            this.buildScope().rebuildDirtyProxies();
        }

        @Override
        public void notifyDescendantInstance(@Nullable WidgetInstance<?> instance, @Nullable Object slot) {
            this.instance().setChild(instance);
        }

        @Override
        public void visitChildren(Visitor visitor) {
            if (this.child != null) {
                visitor.visit(this.child);
            }
        }
    }

    public static class Instance extends OptionalChildWidgetInstance.ShrinkWrap<LayoutBuilder> {

        private Consumer<Constraints> callback;

        public Instance(LayoutBuilder widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.host().notifySubtreeRebuild();
            this.callback.accept(constraints);

            super.doLayout(constraints);
        }
    }

    @FunctionalInterface
    public interface Callback {
        Widget build(BuildContext context, Constraints constraints);
    }
}
