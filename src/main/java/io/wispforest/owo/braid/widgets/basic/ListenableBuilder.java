package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Listenable;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListenableBuilder extends StatefulWidget {

    public final Listenable listenable;
    public final ListenableBuilderWithChildFunction builder;
    public final @Nullable Widget child;

    public ListenableBuilder(Listenable listenable, ListenableBuilderFunction builder) {
        this.listenable = listenable;
        this.builder = (context, $) -> builder.build(context);
        this.child = null;
    }

    public ListenableBuilder(Listenable listenable, ListenableBuilderWithChildFunction builder, @NotNull Widget child) {
        this.listenable = listenable;
        this.builder = builder;
        this.child = child;
    }

    @Override
    public WidgetState<ListenableBuilder> createState() {
        return new State();
    }

    public static class State extends WidgetState<ListenableBuilder> {

        private final Runnable listener = () -> this.setState(() -> {});

        @Override
        public void init() {
            this.widget().listenable.addListener(this.listener);
        }

        @Override
        public void didUpdateWidget(ListenableBuilder oldWidget) {
            if (this.widget().listenable != oldWidget.listenable) {
                oldWidget.listenable.removeListener(this.listener);
                this.widget().listenable.addListener(this.listener);
            }
        }

        @Override
        public Widget build(BuildContext context) {
            return this.widget().builder.build(context, this.widget().child);
        }

        @Override
        public void dispose() {
            this.widget().listenable.removeListener(this.listener);
        }
    }

    // ---

    @FunctionalInterface
    public interface ListenableBuilderFunction {
        Widget build(BuildContext listenableContext);
    }

    @FunctionalInterface
    public interface ListenableBuilderWithChildFunction {
        Widget build(BuildContext listenableContext, Widget child);
    }
}
