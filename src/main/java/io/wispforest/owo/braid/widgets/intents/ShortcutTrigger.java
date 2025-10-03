package io.wispforest.owo.braid.widgets.intents;

import io.wispforest.owo.braid.core.KeyModifiers;

import java.util.*;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;

public record ShortcutTrigger(Set<Trigger> triggers) {

    public static final ShortcutTrigger LEFT_CLICK = new ShortcutTrigger(Trigger.ofMouse(GLFW_MOUSE_BUTTON_LEFT));
    public static final ShortcutTrigger RIGHT_CLICK = new ShortcutTrigger(Trigger.ofMouse(GLFW_MOUSE_BUTTON_RIGHT));

//    public static final ShortcutTrigger CLICK = new ShortcutTrigger(
//        Trigger.ofMouse(GLFW_MOUSE_BUTTON_LEFT),
//        Trigger.ofKey(GLFW_KEY_SPACE),
//        Trigger.ofKey(GLFW_KEY_ENTER),
//        Trigger.ofKey(GLFW_KEY_KP_ENTER)
//    );
//
//    public static final ShortcutTrigger SECONDARY_CLICK = new ShortcutTrigger(
//        Trigger.ofMouse(GLFW_MOUSE_BUTTON_RIGHT),
//        Trigger.ofMouse(GLFW_MOUSE_BUTTON_LEFT, new KeyModifiers(GLFW_MOD_SHIFT)),
//        Trigger.ofKey(GLFW_KEY_SPACE, new KeyModifiers(GLFW_MOD_SHIFT)),
//        Trigger.ofKey(GLFW_KEY_ENTER, new KeyModifiers(GLFW_MOD_SHIFT)),
//        Trigger.ofKey(GLFW_KEY_KP_ENTER, new KeyModifiers(GLFW_MOD_SHIFT))
//    );
//
    public static final ShortcutTrigger UP = new ShortcutTrigger(
        Trigger.ofKey(GLFW_KEY_UP)
    );

    public static final ShortcutTrigger DOWN = new ShortcutTrigger(
        Trigger.ofKey(GLFW_KEY_DOWN)
    );

    public static final ShortcutTrigger RIGHT = new ShortcutTrigger(
        Trigger.ofKey(GLFW_KEY_RIGHT)
    );

    public static final ShortcutTrigger LEFT = new ShortcutTrigger(
        Trigger.ofKey(GLFW_KEY_LEFT)
    );
//
//    public static final ShortcutTrigger POSITIVE_DIRECTIONS = ShortcutTrigger.of(UP, RIGHT);
//    public static final ShortcutTrigger NEGATIVE_DIRECTIONS = ShortcutTrigger.of(DOWN, LEFT);
//
//    public static final ShortcutTrigger INCREMENT = ShortcutTrigger.of(CLICK, POSITIVE_DIRECTIONS);
//    public static final ShortcutTrigger DECREMENT = ShortcutTrigger.of(SECONDARY_CLICK, NEGATIVE_DIRECTIONS);

    public static ShortcutTrigger of(ShortcutTrigger... triggers) {
        return new ShortcutTrigger(Arrays.stream(triggers).flatMap(actionTrigger -> actionTrigger.triggers.stream()).collect(Collectors.toSet()));
    }

    public static ShortcutTrigger of(ShortcutTrigger actionTrigger, Trigger... triggers) {
        var combinedTriggers = new HashSet<>(actionTrigger.triggers);
        combinedTriggers.addAll(Arrays.asList(triggers));
        return new ShortcutTrigger(combinedTriggers);
    }

    public ShortcutTrigger(Collection<Trigger> triggers) {
        this(Set.copyOf(triggers));
    }

    public ShortcutTrigger(Trigger... triggers) {
        this(Set.of(triggers));
    }

    public boolean isTriggeredByMouseButton(int button, KeyModifiers modifiers) {
        return this.triggers.stream().anyMatch(trigger -> trigger instanceof Trigger.Mouse mouseTrigger && mouseTrigger.isTriggered(button, modifiers));
    }

    public boolean isTriggeredByKeyCode(int keyCode, KeyModifiers modifiers) {
        return this.triggers.stream().anyMatch(trigger -> trigger instanceof Trigger.Key keyTrigger && keyTrigger.isTriggered(keyCode, modifiers));
    }
}
