package io.wispforest.owo.braid.framework.proxy;

import com.google.common.base.Preconditions;
import io.wispforest.owo.braid.framework.instance.MultiChildWidgetInstance;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.widget.InstanceWidget;
import io.wispforest.owo.braid.framework.widget.Key;
import io.wispforest.owo.braid.framework.widget.MultiChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiChildInstanceWidgetProxy extends InstanceWidgetProxy {
    public List<WidgetProxy> children = new ArrayList<>();
    public List<WidgetInstance<?>> childInstances = new ArrayList<>();

    public MultiChildInstanceWidgetProxy(MultiChildInstanceWidget widget) {
        super(widget);
    }

    @Override
    public MultiChildWidgetInstance<? extends InstanceWidget> instance() {
        //noinspection unchecked
        return (MultiChildWidgetInstance<? extends InstanceWidget>) super.instance();
    }

    @Override
    public void visitChildren(Visitor visitor) {
        for (var child : children) {
            visitor.visit(child);
        }
    }

    @Override
    public void updateWidget(Widget newWidget) {
        super.updateWidget(newWidget);
        rebuild(true);
    }

    @Override
    public void doRebuild() {
        super.doRebuild();
        var newWidgets = ((MultiChildInstanceWidget) this.widget()).children;

        var newChildrenTop = 0;
        var oldChildrenTop = 0;
        var newChildrenBottom = newWidgets.size() - 1;
        var oldChildrenBottom = this.children.size() - 1;

        var newChildren = Stream.<WidgetProxy>generate(() -> null).limit(newWidgets.size()).collect(Collectors.toList());

        // we already set up the new child instance list, so that any
        // notifyDescendantInstance invocations caused by the below
        // refreshChild calls always index into the correct list
        this.childInstances = Stream.<WidgetInstance<?>>generate(() -> null).limit(newChildren.size()).collect(Collectors.toList());
        copyInto(this.childInstances, 0, this.instance().children, 0, Math.min(this.childInstances.size(), this.instance().children.size()));

        if (this.instance().children.size() > this.childInstances.size()) {
            this.instance().markNeedsLayout();
        }
        this.instance().children = this.childInstances;

        // sync from the top
        while ((oldChildrenTop <= oldChildrenBottom) && (newChildrenTop <= newChildrenBottom)) {
            var oldChild = this.children.get(oldChildrenTop);
            var newWidget = newWidgets.get(newChildrenTop);

            if (!Widget.canUpdate(oldChild.widget(), newWidget)) {
                break;
            }

            newChildren.set(newChildrenTop, this.refreshChild(oldChild, newWidget, newChildrenTop));
            Preconditions.checkNotNull(this.childInstances.get(newChildrenTop));

            oldChildrenTop++;
            newChildrenTop++;
        }

        // scan from the bottom
        while ((oldChildrenTop <= oldChildrenBottom) && (newChildrenTop <= newChildrenBottom)) {
            var oldChild = this.children.get(oldChildrenTop);
            var newWidget = newWidgets.get(newChildrenTop);

            if (!Widget.canUpdate(oldChild.widget(), newWidget)) {
                break;
            }

            oldChildrenTop++;
            newChildrenTop++;
        }

        // scan middle, store keyed and disposed un-keyed

        var hasOldChildren = oldChildrenTop <= oldChildrenBottom;
        Map<Key, WidgetProxy> keyedOldChildren = null;

        if (hasOldChildren) {
            keyedOldChildren = new HashMap<>();
            while (oldChildrenTop <= oldChildrenBottom) {
                var oldChild = this.children.get(oldChildrenTop);
                var key = oldChild.widget().key();

                if (key != null) {
                    keyedOldChildren.put(key, oldChild);
                } else {
                    oldChild.unmount();
                }

                oldChildrenTop++;
            }
        }

        // sync middle, updating keyed

        while (newChildrenTop <= newChildrenBottom) {
            WidgetProxy oldChild = null;
            var newWidget = newWidgets.get(newChildrenTop);

            if (hasOldChildren) {
                var key = newWidget.key();
                if (key != null) {
                    oldChild = keyedOldChildren.get(key);
                    if (oldChild != null) {
                        if (Widget.canUpdate(oldChild.widget(), newWidget)) {
                            keyedOldChildren.remove(key);
                        } else {
                            oldChild = null;
                        }
                    }
                }
            }

            newChildren.set(newChildrenTop, this.refreshChild(oldChild, newWidget, newChildrenTop));
            Preconditions.checkNotNull(this.childInstances.get(newChildrenTop));

            newChildrenTop++;
        }

        newChildrenBottom = newWidgets.size() - 1;
        oldChildrenBottom = this.children.size() - 1;

        while ((oldChildrenTop <= oldChildrenBottom) && (newChildrenTop <= newChildrenBottom)) {
            var oldChild = this.children.get(oldChildrenTop);
            var newWidget = newWidgets.get(newChildrenTop);

            newChildren.set(newChildrenTop, this.refreshChild(oldChild, newWidget, newChildrenTop));
            Preconditions.checkNotNull(this.childInstances.get(newChildrenTop));

            oldChildrenTop++;
            newChildrenTop++;
        }

        // dispose keyed proxies that were not reused
        if (hasOldChildren && !keyedOldChildren.isEmpty()) {
            for (var proxy : keyedOldChildren.values()) {
                proxy.unmount();
            }
        }

        // finally, install new children
        this.children = newChildren;
    }

    @Override
    public void notifyDescendantInstance(@Nullable WidgetInstance<?> instance, @Nullable Object slot) {
        this.instance().insertChild(((Integer) slot).intValue(), instance);
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> void copyInto(List<T> target, int at, List<T> source, int from, int to) {
        var copyCount = to - from;
        for (var i = 0; i < copyCount; i++) {
            target.set(at + i, source.get(from + i));
        }
    }
}
