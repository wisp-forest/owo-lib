package io.wispforest.owo.braid.widgets.basic.action;

import io.wispforest.owo.braid.core.KeyModifiers;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Nullable;

import static org.lwjgl.glfw.GLFW.*;

public record ActionTrigger(
    @Nullable IntSet mouseButtons,
    @Nullable IntSet keyCodes,
    @Nullable KeyModifiers modifiers
) {

    public static final ActionTrigger CLICK = new ActionTrigger(
        IntSet.of(GLFW_MOUSE_BUTTON_LEFT),
        IntSet.of(GLFW_KEY_SPACE, GLFW_KEY_ENTER, GLFW_KEY_KP_ENTER),
        null
    );

    public static final ActionTrigger SECONDARY_CLICK = new ActionTrigger(
        IntSet.of(GLFW_MOUSE_BUTTON_RIGHT),
        IntSet.of(GLFW_KEY_SPACE, GLFW_KEY_ENTER, GLFW_KEY_KP_ENTER),
        new KeyModifiers(GLFW_MOD_SHIFT)
    );

    public boolean isTriggeredByMouseButton(int button) {
        return this.mouseButtons != null && this.mouseButtons.contains(button);
    }

    public boolean isTriggeredByKeyCode(int keyCode, KeyModifiers modifiers) {
        return this.keyCodes != null && this.keyCodes.contains(keyCode) &&
               (this.modifiers == null || this.modifiers.equals(modifiers));
    }
}
