package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;

import java.util.ArrayList;

public class ProxyTreeView extends StatefulWidget {

    public final WidgetProxy viewProxy;
    public final int parentChildCount;

    public ProxyTreeView(WidgetProxy viewProxy) {
        this(viewProxy, 0);
    }

    public ProxyTreeView(WidgetProxy viewProxy, int parentChildCount) {
        this.viewProxy = viewProxy;
        this.parentChildCount = parentChildCount;
    }

    @Override
    public WidgetState<ProxyTreeView> createState() {
        return new State();
    }

    public static class State extends WidgetState<ProxyTreeView> {

        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            var title = new ProxyTitle(widget.viewProxy);

            var children = new ArrayList<WidgetProxy>();
            widget.viewProxy.visitChildren(children::add);

            if (!children.isEmpty()) {
                return new CollapsibleEntry(
                    null,
                    true,
                    widget.parentChildCount,
                    title,
                    children.stream()
                        .map(child -> new ProxyTreeView(child, children.size()))
                        .toList()
                );
            } else {
                return new Row(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new Sized(12, 12, new SpriteWidget(Owo.id("braid_inspector_leaf"))),
                    title
                );
            }
        }
    }
}
