package io.wispforest.owo.braid.widgets.overlay;

import io.wispforest.owo.braid.core.RelativePosition;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class OverlayEntryBuilder {

    final Widget widget;
    final RelativePosition position;
    @Nullable Runnable onRemove = null;
    boolean dismissOverlayOnClick = false;
    boolean occludeHitTest = false;

    public OverlayEntryBuilder(Widget widget, RelativePosition position) {
        this.widget = widget;
        this.position = position;
    }

    public OverlayEntryBuilder onRemove(Runnable onRemove) {
        this.onRemove = onRemove;
        return this;
    }

    public OverlayEntryBuilder dismissOverlayOnClick() {
        this.dismissOverlayOnClick = true;
        return this;
    }

    public OverlayEntryBuilder occludeHitTest() {
        this.occludeHitTest = true;
        return this;
    }
}
