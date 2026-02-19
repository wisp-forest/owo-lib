package io.wispforest.owo.braid.framework.instance;

import io.wispforest.owo.braid.widgets.basic.LayoutBuilder;
import net.minecraft.client.Minecraft;
import org.joml.Vector2dc;

public interface InstanceHost {
    Minecraft client();

    /// Schedule a [WidgetInstance#layout] invocation for `instance`,
    /// to be executed during the next layout pass.
    ///
    /// This function must generally not be called during a layout pass
    /// unless [#notifySubtreeRebuild] has been invoked first since
    /// otherwise we run the risk of laying out some instances twice
    void scheduleLayout(WidgetInstance<?> instance);

    /// Notify the layout scheduler that a widget or proxy subtree
    /// of the current element is (likely) about to rebuild and
    /// subsequently [#scheduleLayout] may be invoked during the
    /// current layout pass
    ///
    /// This is used to implement the [LayoutBuilder] mechanism
    void notifySubtreeRebuild();

    void schedulePostLayoutCallback(Runnable callback);

    Vector2dc cursorPosition();
}
