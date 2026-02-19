package io.wispforest.owo.braid.widgets.intents;

import io.wispforest.owo.braid.core.KeyModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;

public record ShortcutTrigger(Set<Trigger> triggers) {

    public static final ShortcutTrigger LEFT_CLICK = new ShortcutTrigger(Trigger.ofMouse(GLFW_MOUSE_BUTTON_LEFT));
    public static final ShortcutTrigger RIGHT_CLICK = new ShortcutTrigger(Trigger.ofMouse(GLFW_MOUSE_BUTTON_RIGHT));

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

    public static final ShortcutTrigger PAGE_UP = new ShortcutTrigger(
        Trigger.ofKey(GLFW_KEY_PAGE_UP)
    );

    public static final ShortcutTrigger PAGE_DOWN = new ShortcutTrigger(
        Trigger.ofKey(GLFW_KEY_PAGE_DOWN)
    );

    public static final ShortcutTrigger HOME = new ShortcutTrigger(
        Trigger.ofKey(GLFW_KEY_HOME)
    );

    public static final ShortcutTrigger END = new ShortcutTrigger(
        Trigger.ofKey(GLFW_KEY_END)
    );

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

    public ShortcutTrigger withModifiers(@Nullable KeyModifiers modifiers) {
        var triggers = new HashSet<Trigger>();
        for (var trigger : this.triggers) {
            triggers.add(trigger.withModifiers(modifiers));
        }

        return new ShortcutTrigger(triggers);
    }

    public boolean isTriggeredByMouseButton(int button, KeyModifiers modifiers) {
        return this.triggers.stream().anyMatch(trigger -> trigger instanceof Trigger.Mouse mouseTrigger && mouseTrigger.isTriggered(button, modifiers));
    }

    public boolean isTriggeredByKeyCode(int keyCode, KeyModifiers modifiers) {
        return this.triggers.stream().anyMatch(trigger -> trigger instanceof Trigger.Key keyTrigger && keyTrigger.isTriggered(keyCode, modifiers));
    }
}
