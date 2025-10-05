package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.focus.FocusTraversalDirection;
import io.wispforest.owo.braid.widgets.intents.*;

import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class BraidApp extends StatelessWidget {

    public final Widget child;

    public BraidApp(Widget child) {
        this.child = child;
    }

    @Override
    public Widget build(BuildContext context) {
        return new Interactable(
            DEFAULT_SHORTCUTS,
            widget -> widget
                .actions(DEFAULT_ACTIONS)
                .autoFocus(true)
                .skipTraversal(true),
            new Navigator(
                this.child
            )
        );
    }

    // ---

    private static final KeyModifiers SHIFT = new KeyModifiers(GLFW_MOD_SHIFT);

    public static final Map<Class<? extends Intent>, Action<?>> DEFAULT_ACTIONS = Map.of(
        TraverseFocusIntent.class, new TraverseFocusAction()
    );

    public static final Map<List<ShortcutTrigger>, Intent> DEFAULT_SHORTCUTS = Map.of(
        List.of(new ShortcutTrigger(
            Trigger.ofKey(GLFW_KEY_ENTER),
            Trigger.ofKey(GLFW_KEY_KP_ENTER),
            Trigger.ofKey(GLFW_KEY_SPACE)
        )), PrimaryActionIntent.INSTANCE,
        List.of(new ShortcutTrigger(
            Trigger.ofKey(GLFW_KEY_ENTER, SHIFT),
            Trigger.ofKey(GLFW_KEY_KP_ENTER, SHIFT),
            Trigger.ofKey(GLFW_KEY_SPACE, SHIFT)
        )), SecondaryActionIntent.INSTANCE,
        List.of(ShortcutTrigger.UP.withModifiers(null)), new TraverseFocusIntent(FocusTraversalDirection.UP),
        List.of(ShortcutTrigger.DOWN.withModifiers(null)), new TraverseFocusIntent(FocusTraversalDirection.DOWN),
        List.of(ShortcutTrigger.LEFT.withModifiers(null)), new TraverseFocusIntent(FocusTraversalDirection.LEFT),
        List.of(ShortcutTrigger.RIGHT.withModifiers(null)), new TraverseFocusIntent(FocusTraversalDirection.RIGHT),
        List.of(new ShortcutTrigger(Trigger.ofKey(GLFW_KEY_TAB))), new TraverseFocusIntent(FocusTraversalDirection.NEXT),
        List.of(new ShortcutTrigger(Trigger.ofKey(GLFW_KEY_TAB, SHIFT))), new TraverseFocusIntent(FocusTraversalDirection.PREVIOUS)
    );

    // ---

    public static class BaseRoute extends StatelessWidget {

        public final Widget route;

        public BaseRoute(Widget route) {
            this.route = route;
        }

        @Override
        public Widget build(BuildContext context) {
            return this.route;
        }
    }
}
