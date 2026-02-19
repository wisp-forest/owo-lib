package io.wispforest.owo.braid.framework.proxy;

import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class StatefulProxy extends ComposedProxy {

    private final WidgetState<StatefulWidget> state;
    private boolean dependenciesChanged = false;

    public StatefulProxy(StatefulWidget widget) {
        super(widget);

        //noinspection unchecked
        this.state = (WidgetState<StatefulWidget>) widget.createState();
        this.state.widget = (StatefulWidget) this.widget();
        this.state.owner = this;
    }

    public WidgetState<?> state() {
        return this.state;
    }

    @Override
    public void mount(WidgetProxy parent, @Nullable Object slot) {
        super.mount(parent, slot);

        this.state.init();
        this.rebuild();
    }

    @Override
    public void notifyDependenciesChanged() {
        super.notifyDependenciesChanged();
        this.dependenciesChanged = true;
    }

    @Override
    public void unmount() {
        super.unmount();
        this.state.dispose();
    }

    @Override
    public void updateWidget(Widget newWidget) {
        super.updateWidget(newWidget);

        var oldWidget = this.state.widget;
        this.state.widget = (StatefulWidget) newWidget;
        this.state.didUpdateWidget(oldWidget);

        this.rebuild(true);
    }

    @Override
    protected void doRebuild() {
        if (this.dependenciesChanged) {
            this.state.notifyDependenciesChanged();
            this.dependenciesChanged = false;
        }

        var newWidget = this.state.build(this);
        super.doRebuild();

        this.child = this.refreshChild(this.child, newWidget, this.slot());
    }
}
