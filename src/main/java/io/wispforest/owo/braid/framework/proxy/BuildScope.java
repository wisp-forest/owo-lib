package io.wispforest.owo.braid.framework.proxy;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BuildScope {
    private final List<WidgetProxy> dirtyProxies = new ArrayList<>();
    private boolean resortProxies = true;

    private final @Nullable Runnable scheduleRebuild;

    public BuildScope(@Nullable Runnable scheduleRebuild) {
        this.scheduleRebuild = scheduleRebuild;
    }

    public BuildScope() {
        this(null);
    }

    // ---

    public void scheduleRebuild(WidgetProxy proxy) {
        this.dirtyProxies.add(proxy);
        this.resortProxies = true;

        if (this.scheduleRebuild != null) {
            this.scheduleRebuild.run();
        }
    }

    public boolean rebuildDirtyProxies() {
        if (this.dirtyProxies.isEmpty()) return false;

        this.dirtyProxies.sort(Comparator.naturalOrder());

        for (var idx = 0; idx < this.dirtyProxies.size(); idx = this.nextDirtyIndex(idx)) {
            this.dirtyProxies.get(idx).rebuild();
        }

        this.dirtyProxies.clear();
        return true;
    }

    private int nextDirtyIndex(int idx) {
        if (!this.resortProxies) return idx + 1;

        this.dirtyProxies.sort(Comparator.naturalOrder());
        this.resortProxies = false;

        idx++;
        while (idx > 0 && this.dirtyProxies.get(idx - 1).needsRebuild()) {
            idx--;
        }

        return idx;
    }
}
