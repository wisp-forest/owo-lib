package io.wispforest.owo.braid.widgets.intents;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Actions extends StatefulWidget {

    private boolean focusable = true;
    private boolean autoFocus = false;
    private boolean skipTraversal = false;

    private final Map<Class<? extends Intent>, Action<?>> actions;
    public final Widget child;

    public Actions(@Nullable WidgetSetupCallback<Actions> setup, Widget child) {
        this.actions = new HashMap<>();
        this.child = child;
        if (setup != null) setup.setup(this);
    }

    public Actions focusable(boolean focusable) {
        this.assertMutable();
        this.focusable = focusable;
        return this;
    }

    public boolean focusable() {
        return this.focusable;
    }

    public Actions autoFocus(boolean autoFocus) {
        this.assertMutable();
        this.autoFocus = autoFocus;
        return this;
    }

    public boolean autoFocus() {
        return this.autoFocus;
    }

    public Actions skipTraversal(boolean skipTraversal) {
        this.assertMutable();
        this.skipTraversal = skipTraversal;
        return this;
    }

    public boolean skipTraversal() {
        return this.skipTraversal;
    }

    public Actions actions(Map<Class<? extends Intent>, Action<?>> actions) {
        this.assertMutable();
        this.actions.putAll(actions);
        return this;
    }

    public <I extends Intent> Actions addAction(Class<I> intentType, Action<I> action) {
        if (this.actions.containsKey(intentType)) {
            throw new IllegalArgumentException("Duplicate intent type: " + intentType);
        }

        this.actions.put(intentType, action);
        return this;
    }

    public <I extends Intent> Actions addCallbackAction(Class<I> intentType, Action.Callback<I> callback) {
        this.addAction(intentType, Action.callback(callback));
        return this;
    }

    public Map<Class<? extends Intent>, Action<?>> actions() {
        return this.actions;
    }

    @Override
    public WidgetState<Actions> createState() {
        return new State();
    }

    // ---

    public static boolean invoke(BuildContext context, Intent intent) {
        var action = actionForIntent(context, intent);
        if (action != null) {
            action.invoke(context, intent);
            return true;
        }

        return false;
    }

    @SuppressWarnings({"unchecked"})
    public static @Nullable <I extends Intent> Action<I> actionForIntent(BuildContext context, I intent) {
        var intents = context.getAncestor(ActionsProvider.class);
        while (intents != null) {
            var action = intents.state.widget().actions.get(intent.getClass());
            if (action != null && ((Action<I>) action).isActive(context, intent)) {
                break;
            }

            intents = intents.state.context().getAncestor(ActionsProvider.class);
        }

        return intents != null
            ? (Action<I>) intents.state.widget().actions.get(intent.getClass())
            : null;
    }

    // ---

    public static class State extends WidgetState<Actions> {
        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            return new ActionsProvider(
                this,
                widget.focusable
                    ? new Focusable(focusable -> focusable.autoFocus(widget.autoFocus).skipTraversal(this.widget().skipTraversal), widget.child)
                    : widget.child
            );
        }
    }
}

class ActionsProvider extends InheritedWidget {

    public final Actions.State state;

    public ActionsProvider(Actions.State state, Widget child) {
        super(child);
        this.state = state;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return false;
    }
}
