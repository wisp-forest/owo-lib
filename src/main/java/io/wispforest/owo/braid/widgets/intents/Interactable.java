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

public class Interactable extends StatefulWidget {

    private @Nullable MouseArea.EnterCallback enterCallback;
    private @Nullable MouseArea.ExitCallback exitCallback;
    private @Nullable MouseArea.CursorStyleSupplier cursorStyleSupplier;

    private @Nullable Focusable.FocusGainedCallback focusGainedCallback;
    private @Nullable Focusable.FocusLostCallback focusLostCallback;
    private boolean skipTraversal = false;
    private boolean autoFocus = false;

    public final Map<List<ShortcutTrigger>, Intent> shortcuts;

    private final Map<Class<? extends Intent>, Action<?>> actions = new HashMap<>();

    public final Widget child;

    public Interactable(
        @Nullable Map<List<ShortcutTrigger>, Intent> shortcuts,
        WidgetSetupCallback<Interactable> setup,
        Widget child
    ) {
        this.shortcuts = shortcuts != null ? shortcuts : Map.of();
        this.child = child;
        setup.setup(this);
    }

    public static Widget primary(@Nullable Runnable onClick, @Nullable WidgetSetupCallback<Interactable> setup, Widget child) {
        return new Interactable(
            CLICK_SHORTCUT,
            widget -> {
                if (onClick != null) {
                    widget
                        .addAction(PrimaryActionIntent.class, Action.callback((context, intent) -> onClick.run()))
                        .cursorStyle(CursorStyle.HAND);
                }

                if (setup != null) {
                    setup.setup(widget);
                }
            },
            child
        );
    }

    public static Widget primary(Runnable onClick, Widget child) {
        return primary(onClick, null, child);
    }

    // ---

    public Interactable enterCallback(@Nullable MouseArea.EnterCallback enterCallback) {
        this.assertMutable();
        this.enterCallback = enterCallback;
        return this;
    }

    public @Nullable MouseArea.EnterCallback enterCallback() {
        return this.enterCallback;
    }

    public Interactable exitCallback(@Nullable MouseArea.ExitCallback exitCallback) {
        this.assertMutable();
        this.exitCallback = exitCallback;
        return this;
    }

    public @Nullable MouseArea.ExitCallback exitCallback() {
        return this.exitCallback;
    }

    public Interactable cursorStyleSupplier(@Nullable MouseArea.CursorStyleSupplier cursorStyleSupplier) {
        this.assertMutable();
        this.cursorStyleSupplier = cursorStyleSupplier;
        return this;
    }

    public Interactable cursorStyle(@Nullable CursorStyle style) {
        return this.cursorStyleSupplier((x, y) -> style);
    }

    public @Nullable MouseArea.CursorStyleSupplier cursorStyleSupplier() {
        return this.cursorStyleSupplier;
    }

    public Interactable focusGainedCallback(@Nullable Focusable.FocusGainedCallback focusGainedCallback) {
        this.assertMutable();
        this.focusGainedCallback = focusGainedCallback;
        return this;
    }

    public @Nullable Focusable.FocusGainedCallback focusGainedCallback() {
        return this.focusGainedCallback;
    }

    public Interactable focusLostCallback(@Nullable Focusable.FocusLostCallback focusLostCallback) {
        this.assertMutable();
        this.focusLostCallback = focusLostCallback;
        return this;
    }

    public @Nullable Focusable.FocusLostCallback focusLostCallback() {
        return this.focusLostCallback;
    }

    public Interactable skipTraversal(boolean skipTraversal) {
        this.assertMutable();
        this.skipTraversal = skipTraversal;
        return this;
    }

    public boolean skipTraversal() {
        return this.skipTraversal;
    }

    public Interactable autoFocus(boolean autoFocus) {
        this.assertMutable();
        this.autoFocus = autoFocus;
        return this;
    }

    public boolean autoFocus() {
        return this.autoFocus;
    }

    public Interactable actions(Map<Class<? extends Intent>, Action<?>> actions) {
        this.assertMutable();
        this.actions.putAll(actions);
        return this;
    }

    public <I extends Intent> Interactable addAction(Class<I> intentType, Action<I> action) {
        if (this.actions.containsKey(intentType)) {
            throw new IllegalArgumentException("Duplicate intent type: " + intentType);
        }

        this.actions.put(intentType, action);
        return this;
    }

    public <I extends Intent> Interactable addCallbackAction(Class<I> intentType, Action.Callback<I> callback) {
        this.addAction(intentType, Action.callback(callback));
        return this;
    }

    public Map<Class<? extends Intent>, Action<?>> actions() {
        return this.actions;
    }

    @Override
    public WidgetState<Interactable> createState() {
        return new State();
    }

    // ---

    private static final Map<List<ShortcutTrigger>, Intent> CLICK_SHORTCUT = Map.of(
        List.of(ShortcutTrigger.LEFT_CLICK), PrimaryActionIntent.INSTANCE
    );

    // ---

    public static class State extends WidgetState<Interactable> {
        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            return new Actions(
                actions -> actions
                    .focusable(false)
                    .actions(this.widget().actions),
                new Shortcuts(
                    widget.shortcuts,
                    shortcuts -> shortcuts
                        .enterCallback(this.widget().enterCallback)
                        .exitCallback(this.widget().exitCallback)
                        .cursorStyleSupplier(this.widget().cursorStyleSupplier)
                        .focusGainedCallback(this.widget().focusGainedCallback)
                        .focusLostCallback(this.widget().focusLostCallback)
                        .skipTraversal(this.widget().skipTraversal)
                        .autoFocus(this.widget().autoFocus),
                    widget.child
                )
            );
        }
    }
}
