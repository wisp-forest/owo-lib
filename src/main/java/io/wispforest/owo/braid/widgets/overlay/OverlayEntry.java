package io.wispforest.owo.braid.widgets.overlay;

import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class OverlayEntry {

    private final Overlay.State owner;
    final @Nullable Runnable onRemove;
    final UUID uuid = UUID.randomUUID();

    public Widget widget;
    public boolean dismissOnOverlayClick;
    public boolean occludeHitTest;

    public double x;
    public double y;

    OverlayEntry(Overlay.State owner, @Nullable Runnable onRemove, Widget widget, boolean dismissOnOverlayClick, boolean occludeHitTest, double x, double y) {
        this.owner = owner;
        this.onRemove = onRemove;
        this.widget = widget;
        this.dismissOnOverlayClick = dismissOnOverlayClick;
        this.occludeHitTest = occludeHitTest;
        this.x = x;
        this.y = y;
    }

    // ---

    public void setState(Runnable fn) {
        this.owner.setState(fn);
    }

    public void remove() {
        this.owner.setState(() -> {
            if (this.onRemove != null) this.onRemove.run();
            this.owner.entries.remove(this);
        });
    }
}
