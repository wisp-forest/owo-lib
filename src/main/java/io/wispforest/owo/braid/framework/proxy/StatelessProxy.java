package io.wispforest.owo.braid.framework.proxy;

import io.wispforest.owo.braid.framework.widget.StatelessWidget;
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
    public void updateSlot(@Nullable Object newSlot) {
        super.updateSlot(newSlot);
        this.rebuild(true);
    }

    @Override
    protected void doRebuild() {
        var newWidget = ((StatelessWidget) this.widget()).build(this);
        this.child = this.refreshChild(this.child, newWidget, this.slot());

        super.doRebuild();
    }
}
