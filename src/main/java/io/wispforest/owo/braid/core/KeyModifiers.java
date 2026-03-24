package io.wispforest.owo.braid.core;

import it.unimi.dsi.fastutil.ints.IntList;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.*;

/// An abstraction around the key modifier bitmask used by GLFW
/// @see <a href="https://www.glfw.org/docs/latest/group__mods.html">GLFW Documentation</a>
public record KeyModifiers(int bitMask) {
    public static final KeyModifiers NONE = new KeyModifiers(0);

    /// Is [GLFW#GLFW_KEY_LEFT_SHIFT] or [GLFW#GLFW_KEY_RIGHT_SHIFT] currently held?
    public boolean shift() {
        return (this.bitMask & GLFW_MOD_SHIFT) != 0;
    }

    /// Is [GLFW#GLFW_KEY_LEFT_CONTROL] or [GLFW#GLFW_KEY_RIGHT_CONTROL] currently held?
    public boolean ctrl() {
        return (this.bitMask & GLFW_MOD_CONTROL) != 0;
    }

    /// Is [GLFW#GLFW_KEY_LEFT_ALT] or [GLFW#GLFW_KEY_RIGHT_ALT] currently held?
    public boolean alt() {
        return (this.bitMask & GLFW_MOD_ALT) != 0;
    }

    /// Is [GLFW#GLFW_KEY_LEFT_SUPER] or [GLFW#GLFW_KEY_RIGHT_SUPER] currently held?<br>
    /// Known as the "Windows" key on Windows,<br>
    /// the "Command" key on macOS,<br>
    /// and the "Super" or "Meta" key on Linux
    public boolean meta() {
        return (this.bitMask & GLFW_MOD_SUPER) != 0;
    }

    /// Is Caps Lock currently active?
    public boolean capsLock() {
        return (this.bitMask & GLFW_MOD_CAPS_LOCK) != 0;
    }

    /// Is Num Lock currently active?
    public boolean numLock() {
        return (this.bitMask & GLFW_MOD_NUM_LOCK) != 0;
    }

    /// Checks if the given key code is a modifier key
    ///
    /// **Note:** Does not include Caps Lock or Num Lock because they do not need to be held down to be active
    public static boolean isModifier(int keyCode) {
        return MODIFIER_KEYS.contains(keyCode);
    }

    //FIXME: DOCUMENT
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
