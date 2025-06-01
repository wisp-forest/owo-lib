package io.wispforest.owo.braid.framework.instance;

import io.wispforest.owo.braid.core.cursor.CursorStyle;
import org.jetbrains.annotations.Nullable;

public interface MouseListener {
    default @Nullable CursorStyle cursorStyleAt(double x, double y) {
        return null;
    }

    default boolean onMouseDown(double x, double y, int button) {
        return false;
    }
    default boolean onMouseUp(double x, double y, int button) {
        return false;
    }

    default void onMouseEnter() {}
    default void onMouseMove(double toX, double toY) {}
    default void onMouseExit() {}
    default void onMouseDragStart(int button) {}
    default void onMouseDrag(double x, double y, double dx, double dy) {}
    default void onMouseDragEnd() {}

    default boolean onMouseScroll(double x, double y, double horizontal, double vertical) {
        return false;
    }
}
