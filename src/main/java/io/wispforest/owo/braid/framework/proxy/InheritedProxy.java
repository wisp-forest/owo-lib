package io.wispforest.owo.braid.framework.proxy;

import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InheritedProxy extends ComposedProxy {

    private final List<WidgetProxy> dependents = new ArrayList<>();

    public InheritedProxy(InheritedWidget widget) {
        super(widget);
    }

    public void addDependent(WidgetProxy dependent) {
        this.dependents.add(dependent);
    }

    public void removeDependent(WidgetProxy dependent) {
        this.dependents.remove(dependent);
    }

    @Override
    public void mount(WidgetProxy parent, @Nullable Object slot) {
        super.mount(parent, slot);
        this.rebuild();
    }

    @Override
    public void updateWidget(Widget newWidget) {
        var shouldUpdate = ((InheritedWidget) this.widget()).mustRebuildDependents((InheritedWidget) newWidget);

        super.updateWidget(newWidget);

        this.rebuild(true);
        if (shouldUpdate) {
            for (var dependent : this.dependents) {
                dependent.notifyDependenciesChanged();
            }
        }
    }

    @Override
    protected void doRebuild() {
        super.doRebuild();
        this.child = this.refreshChild(this.child, ((InheritedWidget) this.widget()).child, this.slot());
    }
}
