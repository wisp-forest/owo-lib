package io.wispforest.owo.braid.widgets.ifdragissocoolthenwheresdrag2;

import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import org.jetbrains.annotations.Nullable;

public class DragSourceOLD<T> extends StatefulWidget {

    public @Nullable final T data;

    private @Nullable Widget feedback;
    private @Nullable Widget childWhenDragging;

    private @Nullable MouseArea.DragStartCallback dragStartCallback;
    private @Nullable MouseArea.DragCallback dragCallback;
    private @Nullable MouseArea.DragEndCallback dragEndCallback;

    private @Nullable DragSuccessCallback dragSuccessCallback;
    private @Nullable DragFailCallback dragFailCallback;

    private DragValidator dragValidator = DragValidator.DEFAULT;


    private final Widget child;

    public DragSourceOLD(
        @Nullable T data,
        WidgetSetupCallback<DragSourceOLD<T>> setupCallback,
        Widget child
    ) {
        this.data = data;
        setupCallback.setup(this);
        this.child = child;
    }

    public DragSourceOLD<T> feedback(@Nullable Widget feedback) {
        this.assertMutable();
        this.feedback = feedback;
        return this;
    }

    public @Nullable Widget feedback() {
        return this.feedback;
    }

    public DragSourceOLD<T> childWhenDragging(@Nullable Widget childWhenDragging) {
        this.assertMutable();
        this.childWhenDragging = childWhenDragging;
        return this;
    }

    public @Nullable Widget childWhenDragging() {
        return this.childWhenDragging;
    }

    public DragSourceOLD<T> dragStartCallback(@Nullable MouseArea.DragStartCallback dragStartCallback) {
        this.assertMutable();
        this.dragStartCallback = dragStartCallback;
        return this;
    }

    public @Nullable MouseArea.DragStartCallback dragStartCallback() {
        return this.dragStartCallback;
    }

    public DragSourceOLD<T> dragCallback(@Nullable MouseArea.DragCallback dragCallback) {
        this.assertMutable();
        this.dragCallback = dragCallback;
        return this;
    }

    public @Nullable MouseArea.DragCallback dragCallback() {
        return this.dragCallback;
    }

    public DragSourceOLD<T> dragEndCallback(@Nullable MouseArea.DragEndCallback dragEndCallback) {
        this.assertMutable();
        this.dragEndCallback = dragEndCallback;
        return this;
    }

    public @Nullable MouseArea.DragEndCallback dragEndCallback() {
        return this.dragEndCallback;
    }

    public DragSourceOLD<T> dragSuccessCallback(@Nullable DragSuccessCallback dragSuccessCallback) {
        this.assertMutable();
        this.dragSuccessCallback = dragSuccessCallback;
        return this;
    }

    public @Nullable DragSuccessCallback dragSuccessCallback() {
        return this.dragSuccessCallback;
    }

    public DragSourceOLD<T> dragFailCallback(@Nullable DragFailCallback dragFailCallback) {
        this.assertMutable();
        this.dragFailCallback = dragFailCallback;
        return this;
    }

    public @Nullable DragFailCallback dragFailCallback() {
        return this.dragFailCallback;
    }

    public DragSourceOLD<T> canDrag(DragValidator dragValidator) {
        this.assertMutable();
        this.dragValidator = dragValidator;
        return this;
    }

    public DragValidator canDrag() {
        return this.dragValidator;
    }

    @FunctionalInterface
    public interface DragSuccessCallback {
        void onDragSuccess();
    }

    @FunctionalInterface
    public interface DragFailCallback {
        void onDragFail();
    }

    @FunctionalInterface
    public interface DragValidator {
        DragValidator DEFAULT = (button, modifiers) -> button == 0;

        boolean canDrag(int button, KeyModifiers modifiers);
    }

    @Override
    public WidgetState<DragSourceOLD<T>> createState() {
        return new State<>();
    }

    public static class State<T> extends WidgetState<DragSourceOLD<T>> {
        private boolean dragging = false;

        @Override
        public Widget build(BuildContext context) {
            return new MouseArea(
                widget -> widget
                    .dragStartCallback((button, modifiers) ->{
                        if (!this.widget().dragValidator.canDrag(button, modifiers)) return;
                        this.dragging = true;
                    })
                    .dragCallback(this.widget().dragCallback)
                    .dragEndCallback(this.widget().dragEndCallback),
                this.dragging && this.widget().childWhenDragging() != null
                    ? this.widget().childWhenDragging()
                    : this.widget().child
            );
        }
    }
}
