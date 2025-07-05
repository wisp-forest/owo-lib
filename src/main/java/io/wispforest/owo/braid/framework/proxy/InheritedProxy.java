package io.wispforest.owo.braid.framework.proxy;

import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InheritedProxy extends ComposedProxy {

    private final List<WidgetProxy> dependents = new ArrayList<>();

    public InheritedProxy(InheritedWidget widget) {
        super(widget);
    }

    public void addDependency(WidgetProxy dependent, @Nullable Object dependency) {
        this.dependents.add(dependent);
    }

    public void removeDependent(WidgetProxy dependent) {
        this.dependents.remove(dependent);
    }

    protected boolean mustRebuildDependent(WidgetProxy dependent) {
        return true;
    }

    public void notifyDependent(WidgetProxy dependent) {
        dependent.notifyDependenciesChanged();
    }

    @Override
    public void mount(WidgetProxy parent, @Nullable Object slot) {
        super.mount(parent, slot);
        this.inheritedProxies = this.inheritedProxies != null ? new HashMap<>(this.inheritedProxies) : new HashMap<>();
        this.inheritedProxies.put(((InheritedWidget) this.widget()).inheritedKey(), this);

        this.rebuild();
    }

    @Override
    public void updateWidget(Widget newWidget) {
        var shouldUpdate = ((InheritedWidget) this.widget()).mustRebuildDependents((InheritedWidget) newWidget);

        super.updateWidget(newWidget);

        this.rebuild(true);
        if (shouldUpdate) {
            for (var dependent : this.dependents) {
                if (!this.mustRebuildDependent(dependent)) continue;
                this.notifyDependent(dependent);
            }
        }
    }

    @Override
    protected void doRebuild() {
        super.doRebuild();
        this.child = this.refreshChild(this.child, ((InheritedWidget) this.widget()).child, this.slot());
    }
}
