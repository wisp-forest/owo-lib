package io.wispforest.owo.braid.widgets.intents;

import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shortcuts extends StatefulWidget {

    private @Nullable MouseArea.EnterCallback enterCallback;
    private @Nullable MouseArea.ExitCallback exitCallback;
    private @Nullable MouseArea.CursorStyleSupplier cursorStyleSupplier;

    private @Nullable Focusable.FocusGainedCallback focusGainedCallback;
    private @Nullable Focusable.FocusLostCallback focusLostCallback;
    private boolean skipTraversal = false;
    private boolean autoFocus = false;

    public final Map<List<ShortcutTrigger>, Intent> shortcuts;
    public final Widget child;

    public Shortcuts(Map<List<ShortcutTrigger>, Intent> shortcuts, @Nullable WidgetSetupCallback<Shortcuts> setup, Widget child) {
        this.shortcuts = shortcuts;
        this.child = child;
        if (setup != null) setup.setup(this);
    }

    public Shortcuts enterCallback(@Nullable MouseArea.EnterCallback enterCallback) {
        this.assertMutable();
        this.enterCallback = enterCallback;
        return this;
    }

    public @Nullable MouseArea.EnterCallback enterCallback() {
        return this.enterCallback;
    }

    public Shortcuts exitCallback(@Nullable MouseArea.ExitCallback exitCallback) {
        this.assertMutable();
        this.exitCallback = exitCallback;
        return this;
    }

    public @Nullable MouseArea.ExitCallback exitCallback() {
        return this.exitCallback;
    }

    public Shortcuts cursorStyleSupplier(@Nullable MouseArea.CursorStyleSupplier cursorStyleSupplier) {
        this.assertMutable();
        this.cursorStyleSupplier = cursorStyleSupplier;
        return this;
    }

    public Shortcuts cursorStyle(@Nullable CursorStyle style) {
        return this.cursorStyleSupplier((x, y) -> style);
    }

    public @Nullable MouseArea.CursorStyleSupplier cursorStyleSupplier() {
        return this.cursorStyleSupplier;
    }

    public Shortcuts focusGainedCallback(@Nullable Focusable.FocusGainedCallback focusGainedCallback) {
        this.assertMutable();
        this.focusGainedCallback = focusGainedCallback;
        return this;
    }

    public @Nullable Focusable.FocusGainedCallback focusGainedCallback() {
        return this.focusGainedCallback;
    }

    public Shortcuts focusLostCallback(@Nullable Focusable.FocusLostCallback focusLostCallback) {
        this.assertMutable();
        this.focusLostCallback = focusLostCallback;
        return this;
    }

    public @Nullable Focusable.FocusLostCallback focusLostCallback() {
        return this.focusLostCallback;
    }

    public Shortcuts skipTraversal(boolean skipTraversal) {
        this.assertMutable();
        this.skipTraversal = skipTraversal;
        return this;
    }

    public boolean skipTraversal() {
        return this.skipTraversal;
    }

    public Shortcuts autoFocus(boolean autoFocus) {
        this.assertMutable();
        this.autoFocus = autoFocus;
        return this;
    }

    public boolean autoFocus() {
        return this.autoFocus;
    }

    @Override
    public WidgetState<Shortcuts> createState() {
        return new State();
    }

    public static class State extends WidgetState<Shortcuts> {

        private Map<List<ShortcutTrigger>, ShortcutDecoder.Listener> listeners;

        @Override
        public void init() {
            this.buildListeners();
        }

        @Override
        public void didUpdateWidget(Shortcuts oldWidget) {
            this.buildListeners();
        }

        private void buildListeners() {
            this.listeners = new HashMap<>();
            this.widget().shortcuts.forEach((triggers, intent) -> {
                this.listeners.put(triggers, type -> {
                    var sourceContext = switch (type) {
                        case KEY -> Focusable.of(this.context()).primaryFocus().context();
                        case MOUSE -> this.context();
                    };

                    return Actions.invoke(sourceContext, intent);
                });
            });
        }

        @Override
        public Widget build(BuildContext context) {
            return new ShortcutDecoder(
                widget -> widget
                    .shortcuts(this.listeners)
                    .enterCallback(this.widget().enterCallback)
                    .exitCallback(this.widget().exitCallback)
                    .cursorStyleSupplier(this.widget().cursorStyleSupplier)
                    .focusGainedCallback(this.widget().focusGainedCallback)
                    .focusLostCallback(this.widget().focusLostCallback)
                    .skipTraversal(this.widget().skipTraversal)
                    .autoFocus(this.widget().autoFocus),
                this.widget().child
            );
        }
    }
}
