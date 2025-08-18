package io.wispforest.owo.braid.widgets.basic.action;

import io.wispforest.owo.braid.core.KeyModifiers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;

/// Represents a single step in an action sequence.<br>
/// Consists of one or more [Trigger]s, any of which can trigger this step.
public record ActionTrigger(Set<Trigger> triggers) {

    /// An action trigger used for primary interactions, such as activating a button or toggling a checkbox.<br>
    /// Includes left click and the space/enter keys
    public static final ActionTrigger CLICK = new ActionTrigger(
        Trigger.ofMouse(GLFW_MOUSE_BUTTON_LEFT),
        Trigger.ofKey(GLFW_KEY_SPACE),
        Trigger.ofKey(GLFW_KEY_ENTER),
        Trigger.ofKey(GLFW_KEY_KP_ENTER)
    );

    /// An action trigger used for secondary interactions, such as opening context menus or decrementing values.
    /// Includes right click, shift + left click, and shift + space/enter
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

    /// An action trigger that includes both positive directions ([#UP] and [#RIGHT]).<br>
    /// Useful for incrementing values when mouse inputs don't make sense
    /// @see #INCREMENT
    public static final ActionTrigger POSITIVE_DIRECTIONS = ActionTrigger.of(UP, RIGHT);
    /// An action trigger that includes both negative directions ([#DOWN] and [#LEFT]),<br>
    /// Useful for decrementing values when mouse inputs don't make sense
    /// @see #DECREMENT
    public static final ActionTrigger NEGATIVE_DIRECTIONS = ActionTrigger.of(DOWN, LEFT);

    /// An action trigger that includes both vertical directions ([#UP] and [#DOWN]).<br>
    /// I honestly can't think of a use case for this one yet but, if you somehow needed it, you're welcome.
    /// @see #HORIZONTAL_DIRECTIONS
    public static final ActionTrigger VERTICAL_DIRECTIONS = ActionTrigger.of(UP, DOWN);
    /// An action trigger that includes both horizontal directions ([#RIGHT] and [#LEFT]).<br>
    /// I honestly can't think of a use case for this one yet but, if you somehow needed it, you're welcome.
    /// @see #VERTICAL_DIRECTIONS
    public static final ActionTrigger HORIZONTAL_DIRECTIONS = ActionTrigger.of(RIGHT, LEFT);

    /// An action trigger used for incrementing values, such as in a cycling button.<br>
    /// Includes [#CLICK] and [#POSITIVE_DIRECTIONS]
    /// @see io.wispforest.owo.braid.widgets.cycle.RawCyclingButton
    public static final ActionTrigger INCREMENT = ActionTrigger.of(CLICK, POSITIVE_DIRECTIONS);
    /// An action trigger used for decrementing values, such as in a cycling button.<br>
    /// Includes [#SECONDARY_CLICK] and [#NEGATIVE_DIRECTIONS]
    /// @see io.wispforest.owo.braid.widgets.cycle.RawCyclingButton
    public static final ActionTrigger DECREMENT = ActionTrigger.of(SECONDARY_CLICK, NEGATIVE_DIRECTIONS);

    /// Create an ActionTrigger out of multiple other `ActionTrigger`s.<br>
    public static ActionTrigger of(ActionTrigger... triggers) {
        return new ActionTrigger(Arrays.stream(triggers).flatMap(actionTrigger -> actionTrigger.triggers.stream()).collect(Collectors.toSet()));
    }

    /// Create an ActionTrigger by adding additional [Trigger]s to an existing `ActionTrigger`
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
