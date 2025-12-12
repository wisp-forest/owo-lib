package io.wispforest.owo.braid.framework.proxy;

import io.wispforest.owo.Owo;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        if (Owo.DEBUG && this.dirtyProxies.stream().anyMatch(BuildScope::isMissed)) {
            throw new IllegalStateException(
                "missed the following dirty proxies: ["
                    + this.dirtyProxies.stream().filter(BuildScope::isMissed).map(Objects::toString).collect(Collectors.joining(", "))
                    + "]"
            );
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

    // ---

    private static boolean isMissed(WidgetProxy proxy) {
        return proxy.needsRebuild && proxy.lifecycle == WidgetProxy.Lifecycle.LIVE;
    }
}
