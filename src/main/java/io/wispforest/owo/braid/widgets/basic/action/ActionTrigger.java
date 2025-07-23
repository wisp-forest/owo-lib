package io.wispforest.owo.braid.widgets.basic.action;

import io.wispforest.owo.braid.core.KeyModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;

public record ActionTrigger(Set<Trigger> triggers) {

    public static final ActionTrigger CLICK = new ActionTrigger(
        Trigger.ofMouse(GLFW_MOUSE_BUTTON_LEFT),
        Trigger.ofKey(GLFW_KEY_SPACE),
        Trigger.ofKey(GLFW_KEY_ENTER),
        Trigger.ofKey(GLFW_KEY_KP_ENTER)
    );

    public static final ActionTrigger SECONDARY_CLICK = new ActionTrigger(
        Trigger.ofMouse(GLFW_MOUSE_BUTTON_RIGHT),
        Trigger.ofMouse(GLFW_MOUSE_BUTTON_LEFT, new KeyModifiers(GLFW_MOD_SHIFT)),
        Trigger.ofKey(GLFW_KEY_SPACE, new KeyModifiers(GLFW_MOD_SHIFT)),
        Trigger.ofKey(GLFW_KEY_ENTER, new KeyModifiers(GLFW_MOD_SHIFT)),
        Trigger.ofKey(GLFW_KEY_KP_ENTER, new KeyModifiers(GLFW_MOD_SHIFT))
    );

    public static final ActionTrigger UP = new ActionTrigger(
        Trigger.ofKey(GLFW_KEY_UP)
    );

    public static final ActionTrigger DOWN = new ActionTrigger(
        Trigger.ofKey(GLFW_KEY_DOWN)
    );

    public static final ActionTrigger RIGHT = new ActionTrigger(
        Trigger.ofKey(GLFW_KEY_RIGHT)
    );

    public static final ActionTrigger LEFT = new ActionTrigger(
        Trigger.ofKey(GLFW_KEY_LEFT)
    );

    public static final ActionTrigger INCREMENT = ActionTrigger.of(
        ActionTrigger.CLICK,
        ActionTrigger.UP,
        ActionTrigger.RIGHT
    );

    public static final ActionTrigger DECREMENT = ActionTrigger.of(
        ActionTrigger.SECONDARY_CLICK,
        ActionTrigger.DOWN,
        ActionTrigger.LEFT
    );

    public static ActionTrigger of(ActionTrigger... triggers) {
        return new ActionTrigger(Arrays.stream(triggers).flatMap(actionTrigger -> actionTrigger.triggers.stream()).collect(Collectors.toSet()));
    }

    public static ActionTrigger of(ActionTrigger actionTrigger, Trigger... triggers) {
        var combinedTriggers = new HashSet<>(actionTrigger.triggers);
        combinedTriggers.addAll(Arrays.asList(triggers));
        return new ActionTrigger(combinedTriggers);
    }

    public ActionTrigger(Collection<Trigger> triggers) {
        this(Set.copyOf(triggers));
    }

    public ActionTrigger(Trigger... triggers) {
        this(Set.of(triggers));
    }

    public boolean isTriggeredByMouseButton(int button, KeyModifiers modifiers) {
        return this.triggers.stream().anyMatch(trigger -> trigger instanceof Trigger.Mouse mouseTrigger && mouseTrigger.isTriggered(button, modifiers));
    }

    public boolean isTriggeredByKeyCode(int keyCode, KeyModifiers modifiers) {
        return this.triggers.stream().anyMatch(trigger -> trigger instanceof Trigger.Key keyTrigger && keyTrigger.isTriggered(keyCode, modifiers));
    }
}
