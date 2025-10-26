package io.wispforest.owo.braid.framework.proxy;

import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class StatelessProxy extends ComposedProxy {
    public StatelessProxy(StatelessWidget widget) {
        super(widget);
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
        var newWidget = ((StatelessWidget) this.widget()).build(this);
        super.doRebuild();

        this.child = this.refreshChild(this.child, newWidget, this.slot());
    }
}
