package io.wispforest.owo.braid.widgets.focus;

import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Builder;
import io.wispforest.owo.braid.widgets.eventstream.BraidEventSource;
import io.wispforest.owo.braid.widgets.eventstream.StreamListenerState;

public class RootFocusScope extends StatefulWidget {

    public final BraidEventSource<KeyDownEvent> onKeyDown;
    public final BraidEventSource<KeyUpEvent> onKeyUp;
    public final BraidEventSource<CharEvent> onChar;
    public final Widget child;

    public RootFocusScope(
        BraidEventSource<KeyDownEvent> onKeyDown,
        BraidEventSource<KeyUpEvent> onKeyUp,
        BraidEventSource<CharEvent> onChar,
        Widget child
    ) {
        this.onKeyDown = onKeyDown;
        this.onKeyUp = onKeyUp;
        this.onChar = onChar;
        this.child = child;
    }

    @Override
    public WidgetState<RootFocusScope> createState() {
        return new State();
    }

    public static class State extends StreamListenerState<RootFocusScope> {

        private FocusScope.State scope;

        @Override
        public void init() {
            this.streamListen(widget -> widget.onKeyDown, event -> this.scope.onKeyDown(event.keyCode, event.modifiers));
            this.streamListen(widget -> widget.onKeyUp, event -> this.scope.onKeyUp(event.keyCode, event.modifiers));
            this.streamListen(widget -> widget.onChar, event -> this.scope.onChar(event.charCode, event.modifiers));
        }

        @Override
        public Widget build(BuildContext context) {
            return new FocusPolicy(
                true,
                new FocusScope(
                    Widget.noSetup(),
                    new Builder(scopeContext -> {
                        if (this.scope == null) {
                            this.scope = FocusScope.State.maybeOf(scopeContext);
                            //noinspection DataFlowIssue
                            this.scope.onFocusChange(FocusLevel.BASE);
                        }

                        return this.widget().child;
                    })
                )
            );
        }
    }

    // ---

    public record KeyDownEvent(int keyCode, KeyModifiers modifiers) {}

    public record KeyUpEvent(int keyCode, KeyModifiers modifiers) {}

    public record CharEvent(int charCode, KeyModifiers modifiers) {}
}
