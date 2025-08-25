package io.wispforest.owo.braid.widgets.collapsible;

@FunctionalInterface
public interface CollapsibleCallback {
    void onToggled(boolean nowCollapsed);
}
