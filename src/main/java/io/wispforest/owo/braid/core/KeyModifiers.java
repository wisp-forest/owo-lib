package io.wispforest.owo.braid.core;

import it.unimi.dsi.fastutil.ints.IntList;

import static org.lwjgl.glfw.GLFW.*;

public record KeyModifiers(int bitMask) {
    public static final KeyModifiers NONE = new KeyModifiers(0);

    public boolean shift() {
        return (this.bitMask & GLFW_MOD_SHIFT) != 0;
    }

    public boolean ctrl() {
        return (this.bitMask & GLFW_MOD_CONTROL) != 0;
    }

    public boolean alt() {
        return (this.bitMask & GLFW_MOD_ALT) != 0;
    }

    public boolean meta() {
        return (this.bitMask & GLFW_MOD_SUPER) != 0;
    }

    public boolean capsLock() {
        return (this.bitMask & GLFW_MOD_CAPS_LOCK) != 0;
    }

    public boolean numLock() {
        return (this.bitMask & GLFW_MOD_NUM_LOCK) != 0;
    }

    public static boolean isModifier(int keyCode) {
        return MODIFIER_KEYS.contains(keyCode);
    }

    public static KeyModifiers both(KeyModifiers a, KeyModifiers b) {
        return new KeyModifiers(a.bitMask | b.bitMask);
    }

    public static final IntList MODIFIER_KEYS = IntList.of(
        GLFW_KEY_LEFT_SHIFT,
        GLFW_KEY_RIGHT_SHIFT,
        GLFW_KEY_LEFT_CONTROL,
        GLFW_KEY_RIGHT_CONTROL,
        GLFW_KEY_LEFT_ALT,
        GLFW_KEY_RIGHT_ALT,
        GLFW_KEY_LEFT_SUPER,
        GLFW_KEY_RIGHT_SUPER
    );
}
