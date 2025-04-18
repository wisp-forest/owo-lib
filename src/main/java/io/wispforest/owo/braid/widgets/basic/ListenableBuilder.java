package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Listenable;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class ListenableBuilder extends StatefulWidget {

    public final Listenable listenable;
    public final Function<Widget, Widget> builder;
    public final @Nullable Widget child;

    public ListenableBuilder(Listenable listenable, Supplier<Widget> builder) {
        this.listenable = listenable;
        this.builder = $ -> builder.get();
        this.child = null;
    }

    public ListenableBuilder(Listenable listenable, Function<Widget, Widget> builder, Widget child) {
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
            return this.widget().builder.apply(this.widget().child);
        }

        @Override
        public void dispose() {
            this.widget().listenable.removeListener(this.listener);
        }
    }
}
