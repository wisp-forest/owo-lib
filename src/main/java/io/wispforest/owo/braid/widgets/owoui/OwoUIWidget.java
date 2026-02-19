package io.wispforest.owo.braid.widgets.owoui;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.basic.Builder;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import io.wispforest.owo.ui.core.ParentUIComponent;

import java.util.function.Supplier;

public class OwoUIWidget extends StatefulWidget {
    private final Supplier<ParentUIComponent> componentSupplier;

    public OwoUIWidget(Supplier<ParentUIComponent> componentSupplier) {
        this.componentSupplier = componentSupplier;
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<OwoUIWidget> {
        private ParentUIComponent component;
        private BuildContext owoUiContext;

        @Override
        public void init() {
            component = this.widget().componentSupplier.get();
        }

        @Override
        public Widget build(BuildContext context) {
            return new Align(
                Alignment.TOP_LEFT,
                new Focusable(
                    widget -> widget
                        .focusLostCallback(() -> ((OwoUIWidgetWrapper.Instance) this.owoUiContext.instance()).onFocusLost())
                        .keyDownCallback((keyCode, modifiers) -> ((OwoUIWidgetWrapper.Instance) this.owoUiContext.instance()).onKeyDown(keyCode, modifiers))
                        .charCallback((charCode, modifiers) -> ((OwoUIWidgetWrapper.Instance) this.owoUiContext.instance()).onChar(charCode, modifiers)),
                    new Builder(owoUiContext -> {
                        this.owoUiContext = owoUiContext;
                        return new OwoUIWidgetWrapper(component);
                    })
                )
            );
        }
    }
}
