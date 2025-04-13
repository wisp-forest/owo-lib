package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.instance.MouseListener;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class MouseArea extends SingleChildInstanceWidget {

    public final @Nullable ClickCallback clickCallback;
    public final @Nullable EnterCallback enterCallback;
    public final @Nullable ExitCallback exitCallback;
    public final @Nullable DragStartCallback dragStartCallback;
    public final @Nullable DragCallback dragCallback;
    public final @Nullable DragEndCallback dragEndCallback;
    public final @Nullable ScrollCallback scrollCallback;
    public final @Nullable CursorStyleSupplier cursorStyleSupplier;

    public MouseArea(
        @Nullable ClickCallback clickCallback,
        @Nullable EnterCallback enterCallback,
        @Nullable ExitCallback exitCallback,
        @Nullable DragStartCallback dragStartCallback,
        @Nullable DragCallback dragCallback,
        @Nullable DragEndCallback dragEndCallback,
        @Nullable ScrollCallback scrollCallback,
        @Nullable CursorStyleSupplier cursorStyleSupplier,
        Widget child
    ) {
        super(child);
        this.clickCallback = clickCallback;
        this.enterCallback = enterCallback;
        this.exitCallback = exitCallback;
        this.dragStartCallback = dragStartCallback;
        this.dragCallback = dragCallback;
        this.dragEndCallback = dragEndCallback;
        this.scrollCallback = scrollCallback;
        this.cursorStyleSupplier = cursorStyleSupplier;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    @FunctionalInterface
    public interface ClickCallback {
        void onClick(double x, double y);
    }

    @FunctionalInterface
    public interface EnterCallback {
        void onMouseEnter();
    }

    @FunctionalInterface
    public interface ExitCallback {
        void onMouseExit();
    }

    @FunctionalInterface
    public interface DragStartCallback {
        void onDragStart();
    }

    @FunctionalInterface
    public interface DragCallback {
        void onDrag(double x, double y, double dx, double dy);
    }

    @FunctionalInterface
    public interface DragEndCallback {
        void onDragEnd();
    }

    @FunctionalInterface
    public interface ScrollCallback {
        void onScroll(double horizontal, double vertical);
    }

    @FunctionalInterface
    public interface CursorStyleSupplier {
        @Nullable CursorStyle getCursorStyle(double x, double y);
    }

    public static class Instance extends SingleChildWidgetInstance<MouseArea> implements MouseListener {

        public Instance(MouseArea widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.sizeToChild(constraints, this.child);
        }

        @Override
        public @Nullable CursorStyle cursorStyleAt(double x, double y) {
            if (this.widget.cursorStyleSupplier == null) return null;
            return this.widget.cursorStyleSupplier.getCursorStyle(x, y);
        }

        @Override
        public boolean onMouseDown(double x, double y) {
            if (this.widget.clickCallback != null) {
                this.widget.clickCallback.onClick(x, y);
                return true;
            }

            return this.widget.dragCallback != null;
        }

        @Override
        public void onMouseEnter() {
            if (this.widget.enterCallback != null) this.widget.enterCallback.onMouseEnter();
        }

        @Override
        public void onMouseExit() {
            if (this.widget.exitCallback != null) this.widget.exitCallback.onMouseExit();
        }

        @Override
        public void onMouseDragStart() {
            if (this.widget.dragStartCallback != null) this.widget.dragStartCallback.onDragStart();
        }

        @Override
        public void onMouseDrag(double x, double y, double dx, double dy) {
            if (this.widget.dragCallback != null) this.widget.dragCallback.onDrag(x, y, dx, dy);
        }

        @Override
        public void onMouseDragEnd() {
            if (this.widget.dragEndCallback != null) this.widget.dragEndCallback.onDragEnd();
        }

        @Override
        public boolean onMouseScroll(double x, double y, double vertical, double horizontal) {
            if (this.widget.scrollCallback != null) {
                this.widget.scrollCallback.onScroll(horizontal, vertical);
                return true;
            }

            return false;
        }
    }
}
