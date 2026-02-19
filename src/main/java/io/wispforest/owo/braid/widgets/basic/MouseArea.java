package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.instance.MouseListener;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import org.jetbrains.annotations.Nullable;

public class MouseArea extends SingleChildInstanceWidget {

    private @Nullable ClickCallback clickCallback;
    private @Nullable ReleaseCallback releaseCallback;
    private @Nullable EnterCallback enterCallback;
    private @Nullable MoveCallback moveCallback;
    private @Nullable ExitCallback exitCallback;
    private @Nullable DragStartCallback dragStartCallback;
    private @Nullable DragCallback dragCallback;
    private @Nullable DragEndCallback dragEndCallback;
    private @Nullable ScrollCallback scrollCallback;
    private @Nullable CursorStyleSupplier cursorStyleSupplier;

    public MouseArea(
        WidgetSetupCallback<MouseArea> setupCallback,
        Widget child
    ) {
        super(child);
        setupCallback.setup(this);
    }

    public MouseArea clickCallback(@Nullable ClickCallback clickCallback) {
        this.assertMutable();
        this.clickCallback = clickCallback;
        return this;
    }

    public @Nullable ClickCallback clickCallback() {
        return this.clickCallback;
    }

    public MouseArea releaseCallback(@Nullable ReleaseCallback releaseCallback) {
        this.assertMutable();
        this.releaseCallback = releaseCallback;
        return this;
    }

    public @Nullable ReleaseCallback releaseCallback() {
        return this.releaseCallback;
    }

    public MouseArea enterCallback(@Nullable EnterCallback enterCallback) {
        this.assertMutable();
        this.enterCallback = enterCallback;
        return this;
    }

    public @Nullable EnterCallback enterCallback() {
        return this.enterCallback;
    }

    public MouseArea moveCallback(@Nullable MoveCallback moveCallback) {
        this.assertMutable();
        this.moveCallback = moveCallback;
        return this;
    }

    public @Nullable MoveCallback moveCallback() {
        return this.moveCallback;
    }

    public MouseArea exitCallback(@Nullable ExitCallback exitCallback) {
        this.assertMutable();
        this.exitCallback = exitCallback;
        return this;
    }

    public @Nullable ExitCallback exitCallback() {
        return this.exitCallback;
    }

    public MouseArea dragStartCallback(@Nullable DragStartCallback dragStartCallback) {
        this.assertMutable();
        this.dragStartCallback = dragStartCallback;
        return this;
    }

    public @Nullable DragStartCallback dragStartCallback() {
        return this.dragStartCallback;
    }

    public MouseArea dragCallback(@Nullable DragCallback dragCallback) {
        this.assertMutable();
        this.dragCallback = dragCallback;
        return this;
    }

    public @Nullable DragCallback dragCallback() {
        return this.dragCallback;
    }

    public MouseArea dragEndCallback(@Nullable DragEndCallback dragEndCallback) {
        this.assertMutable();
        this.dragEndCallback = dragEndCallback;
        return this;
    }

    public @Nullable DragEndCallback dragEndCallback() {
        return this.dragEndCallback;
    }

    public MouseArea scrollCallback(@Nullable ScrollCallback scrollCallback) {
        this.assertMutable();
        this.scrollCallback = scrollCallback;
        return this;
    }

    public @Nullable ScrollCallback scrollCallback() {
        return this.scrollCallback;
    }

    public MouseArea cursorStyleSupplier(@Nullable CursorStyleSupplier cursorStyleSupplier) {
        this.assertMutable();
        this.cursorStyleSupplier = cursorStyleSupplier;
        return this;
    }

    public MouseArea cursorStyle(@Nullable CursorStyle style) {
        return this.cursorStyleSupplier((x, y) -> style);
    }

    public @Nullable CursorStyleSupplier cursorStyleSupplier() {
        return this.cursorStyleSupplier;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    @FunctionalInterface
    public interface ClickCallback {
        boolean onClick(double x, double y, int button, KeyModifiers modifiers);
    }

    @FunctionalInterface
    public interface ReleaseCallback {
        boolean onRelease(double x, double y, int button, KeyModifiers modifiers);
    }

    @FunctionalInterface
    public interface EnterCallback {
        void onMouseEnter();
    }

    @FunctionalInterface
    public interface MoveCallback {
        void onMouseMove(double toX, double toY);
    }

    @FunctionalInterface
    public interface ExitCallback {
        void onMouseExit();
    }

    @FunctionalInterface
    public interface DragStartCallback {
        void onDragStart(int button, KeyModifiers modifiers);
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
        boolean onScroll(double horizontal, double vertical);
    }

    @FunctionalInterface
    public interface CursorStyleSupplier {
        @Nullable CursorStyle getCursorStyle(double x, double y);
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<MouseArea> implements MouseListener {

        public Instance(MouseArea widget) {
            super(widget);
        }

        @Override
        public @Nullable CursorStyle cursorStyleAt(double x, double y) {
            if (this.widget.cursorStyleSupplier == null) return null;
            return this.widget.cursorStyleSupplier.getCursorStyle(x, y);
        }

        @Override
        public boolean onMouseDown(double x, double y, int button, KeyModifiers modifiers) {
            if (this.widget.clickCallback != null) {
                return this.widget.clickCallback.onClick(x, y, button, modifiers);
            }

            return this.widget.dragCallback != null;
        }

        @Override
        public boolean onMouseUp(double x, double y, int button, KeyModifiers modifiers) {
            if (this.widget.releaseCallback != null) {
                return this.widget.releaseCallback.onRelease(x, y, button, modifiers);
            }

            return this.widget.dragEndCallback != null;
        }

        @Override
        public void onMouseEnter() {
            if (this.widget.enterCallback != null) this.widget.enterCallback.onMouseEnter();
        }

        @Override
        public void onMouseMove(double toX, double toY) {
            if (this.widget.moveCallback != null) this.widget.moveCallback.onMouseMove(toX, toY);
        }

        @Override
        public void onMouseExit() {
            if (this.widget.exitCallback != null) this.widget.exitCallback.onMouseExit();
        }

        @Override
        public void onMouseDragStart(int button, KeyModifiers modifiers) {
            if (this.widget.dragStartCallback != null) this.widget.dragStartCallback.onDragStart(button, modifiers);
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
        public boolean onMouseScroll(double x, double y, double horizontal, double vertical) {
            if (this.widget.scrollCallback != null) {
                return this.widget.scrollCallback.onScroll(horizontal, vertical);
            }

            return false;
        }
    }
}
