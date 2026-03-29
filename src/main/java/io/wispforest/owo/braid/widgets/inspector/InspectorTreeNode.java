package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface InspectorTreeNode {
    List<InspectorTreeNode> children();
    Widget buildTitle();
    int depth();

    @Nullable WidgetInstance<?> instance();

    record Instance(WidgetInstance<?> instance) implements InspectorTreeNode {
        @Override
        public List<InspectorTreeNode> children() {
            var c = new ArrayList<InspectorTreeNode>();
            instance.visitChildren(child -> c.add(new Instance(child)));
            return c;
        }

        @Override
        public Widget buildTitle() {
            return new InstanceTitle(instance);
        }

        @Override
        public int depth() {
            return instance.depth();
        }

    }

    record Proxy(WidgetProxy proxy) implements InspectorTreeNode {
        @Override
        public List<InspectorTreeNode> children() {
            var c = new ArrayList<InspectorTreeNode>();
            proxy.visitChildren(child -> c.add(new Proxy(child)));
            return c;
        }

        @Override
        public Widget buildTitle() {
            return new ProxyTitle(proxy);
        }

        @Override
        public int depth() {
            return proxy.depth();
        }

        @Override
        public WidgetInstance<?> instance() {
            return proxy.instance();
        }
    }
}
