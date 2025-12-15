package io.wispforest.owo.braid.widgets;

import com.google.common.collect.ImmutableMap;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.focus.FocusTraversalDirection;
import io.wispforest.owo.braid.widgets.intents.*;
import io.wispforest.owo.braid.widgets.textinput.*;
import net.minecraft.util.Util;

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
                .skipTraversal(true),
            new Shortcuts(
                DEFAULT_TEXT_SHORTCUTS,
                widget -> widget
                    .autoFocus(true)
                    .skipTraversal(true),
                new Navigator(
                    this.child
                )
            )
        );
    }

    // ---

    private static final KeyModifiers SHIFT = new KeyModifiers(GLFW_MOD_SHIFT);
    private static final KeyModifiers CTRL = new KeyModifiers(GLFW_MOD_CONTROL);
    private static final KeyModifiers SHIFT_AND_CTRL = KeyModifiers.both(SHIFT, CTRL);

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

    public static final Map<List<ShortcutTrigger>, Intent> DEFAULT_TEXT_SHORTCUTS = Util.make(() -> {
        var builder = new ImmutableMap.Builder<List<ShortcutTrigger>, Intent>();

        builder.put(List.of(new ShortcutTrigger(
            Trigger.ofKey(GLFW_KEY_ENTER),
            Trigger.ofKey(GLFW_KEY_KP_ENTER)
        ).withModifiers(null)), InsertNewlineIntent.INSTANCE);
        builder.put(List.of(new ShortcutTrigger(Trigger.ofKey(GLFW_KEY_TAB))), InsertTabIntent.INSTANCE);
        builder.put(List.of(new ShortcutTrigger(Trigger.ofKey(GLFW_KEY_BACKSPACE))), new DeleteTextIntent(false, false));
        builder.put(List.of(new ShortcutTrigger(Trigger.ofKey(GLFW_KEY_BACKSPACE)).withModifiers(CTRL)), new DeleteTextIntent(false, true));
        builder.put(List.of(new ShortcutTrigger(Trigger.ofKey(GLFW_KEY_DELETE))), new DeleteTextIntent(true, false));
        builder.put(List.of(new ShortcutTrigger(Trigger.ofKey(GLFW_KEY_DELETE)).withModifiers(CTRL)), new DeleteTextIntent(true, true));
        builder.put(List.of(new ShortcutTrigger(Trigger.ofKey(GLFW_KEY_DELETE)).withModifiers(SHIFT)), DeleteLineIntent.INSTANCE);
        builder.put(List.of(ShortcutTrigger.UP), new MoveCursorIntent(MoveCursorIntent.Direction.UP, false, false));
        builder.put(List.of(ShortcutTrigger.DOWN), new MoveCursorIntent(MoveCursorIntent.Direction.DOWN, false, false));
        builder.put(List.of(ShortcutTrigger.LEFT), new MoveCursorIntent(MoveCursorIntent.Direction.LEFT, false, false));
        builder.put(List.of(ShortcutTrigger.RIGHT), new MoveCursorIntent(MoveCursorIntent.Direction.RIGHT, false, false));
        builder.put(List.of(ShortcutTrigger.UP.withModifiers(SHIFT)), new MoveCursorIntent(MoveCursorIntent.Direction.UP, false, true));
        builder.put(List.of(ShortcutTrigger.DOWN.withModifiers(SHIFT)), new MoveCursorIntent(MoveCursorIntent.Direction.DOWN, false, true));
        builder.put(List.of(ShortcutTrigger.LEFT.withModifiers(SHIFT)), new MoveCursorIntent(MoveCursorIntent.Direction.LEFT, false, true));
        builder.put(List.of(ShortcutTrigger.RIGHT.withModifiers(SHIFT)), new MoveCursorIntent(MoveCursorIntent.Direction.RIGHT, false, true));
        builder.put(List.of(ShortcutTrigger.UP.withModifiers(CTRL)), new MoveCursorIntent(MoveCursorIntent.Direction.UP, true, false));
        builder.put(List.of(ShortcutTrigger.DOWN.withModifiers(CTRL)), new MoveCursorIntent(MoveCursorIntent.Direction.DOWN, true, false));
        builder.put(List.of(ShortcutTrigger.LEFT.withModifiers(CTRL)), new MoveCursorIntent(MoveCursorIntent.Direction.LEFT, true, false));
        builder.put(List.of(ShortcutTrigger.RIGHT.withModifiers(CTRL)), new MoveCursorIntent(MoveCursorIntent.Direction.RIGHT, true, false));
        builder.put(List.of(ShortcutTrigger.UP.withModifiers(SHIFT_AND_CTRL)), new MoveCursorIntent(MoveCursorIntent.Direction.UP, true, true));
        builder.put(List.of(ShortcutTrigger.DOWN.withModifiers(SHIFT_AND_CTRL)), new MoveCursorIntent(MoveCursorIntent.Direction.DOWN, true, true));
        builder.put(List.of(ShortcutTrigger.LEFT.withModifiers(SHIFT_AND_CTRL)), new MoveCursorIntent(MoveCursorIntent.Direction.LEFT, true, true));
        builder.put(List.of(ShortcutTrigger.RIGHT.withModifiers(SHIFT_AND_CTRL)), new MoveCursorIntent(MoveCursorIntent.Direction.RIGHT, true, true));
        builder.put(List.of(ShortcutTrigger.HOME), new TeleportCursorIntent(true, false));
        builder.put(List.of(ShortcutTrigger.HOME.withModifiers(SHIFT)), new TeleportCursorIntent(true, true));
        builder.put(List.of(ShortcutTrigger.END), new TeleportCursorIntent(false, false));
        builder.put(List.of(ShortcutTrigger.END.withModifiers(SHIFT)), new TeleportCursorIntent(false, true));
        builder.put(List.of(new ShortcutTrigger(Trigger.ofKey(GLFW_KEY_A)).withModifiers(CTRL)), SelectAllIntent.INSTANCE);
        builder.put(List.of(new ShortcutTrigger(Trigger.ofKey(GLFW_KEY_C)).withModifiers(CTRL)), new CopyTextIntent(false));
        builder.put(List.of(new ShortcutTrigger(Trigger.ofKey(GLFW_KEY_X)).withModifiers(CTRL)), new CopyTextIntent(true));
        builder.put(List.of(new ShortcutTrigger(Trigger.ofKey(GLFW_KEY_V)).withModifiers(CTRL)), PasteTextIntent.INSTANCE);

        return builder.build();
    });

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