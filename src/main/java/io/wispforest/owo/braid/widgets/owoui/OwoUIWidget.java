package io.wispforest.owo.braid.widgets.owoui;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.ui.core.ParentComponent;

import java.util.function.Supplier;

public class OwoUIWidget extends StatefulWidget {
    private final Supplier<ParentComponent> componentSupplier;

    public OwoUIWidget(Supplier<ParentComponent> componentSupplier) {
        this.componentSupplier = componentSupplier;
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<OwoUIWidget> {
        private ParentComponent component;

        @Override
        public void init() {
            component = this.widget().componentSupplier.get();
        }

        @Override
        public Widget build(BuildContext context) {
            return new Align(
                Alignment.TOP_LEFT,
                new OwoUIWidgetWrapper(component)
            );
        }
    }
}
