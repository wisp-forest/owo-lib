package io.wispforest.owo.braid.widgets.collapsible;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Padding;

public class LazyCollapsible extends StatefulWidget {
    public final boolean showVerticalRule;

    public final boolean collapsed;
    public final CollapsibleCallback onToggled;

    public final Widget title;
    public final Widget content;

    public LazyCollapsible(boolean showVerticalRule, boolean collapsed, CollapsibleCallback onToggled, Widget title, Widget content) {
        this.showVerticalRule = showVerticalRule;
        this.collapsed = collapsed;
        this.onToggled = onToggled;
        this.title = title;
        this.content = content;
    }

    @Override
    public WidgetState<LazyCollapsible> createState() {
        return new State();
    }

    public static class State extends WidgetState<LazyCollapsible> {

        public boolean expandedOnce = false;

        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            if (!widget.collapsed && !this.expandedOnce) {
                this.expandedOnce = true;
            }

            return new Collapsible(
                widget.showVerticalRule,
                widget.collapsed,
                widget.onToggled,
                widget.title,
                this.expandedOnce ? widget.content : new Padding(Insets.none())
            );
        }
    }
}
