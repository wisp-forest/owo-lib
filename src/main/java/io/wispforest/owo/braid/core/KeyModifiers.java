package io.wispforest.owo.braid.core;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.ints.IntList;



/// An abstraction around the key modifier bitmask used by SDL
/// @see <a href="https://wiki.libsdl.org/SDL3/SDL_Keymod">SDL Documentation</a>
public record KeyModifiers(int bitMask) {
    public static final KeyModifiers NONE = new KeyModifiers(0);

    /// Is [InputConstants#KEY_LSHIFT] or [InputConstants#KEY_RSHIFT] currently held?
    public boolean shift() {
        return (this.bitMask & InputConstants.MOD_SHIFT) != 0;
    }

    /// Is [InputConstants#KEY_LCONTROL] or [InputConstants#KEY_RCONTROL] currently held?
    public boolean ctrl() {
        return (this.bitMask & InputConstants.MOD_CONTROL) != 0;
    }

    /// Is [InputConstants#KEY_LALT] or [InputConstants#KEY_RALT] currently held?
    public boolean alt() {
        return (this.bitMask & InputConstants.MOD_ALT) != 0;
    }

    /// Is [InputConstants#KEY_LGUI] or [InputConstants#KEY_RGUI] currently held?<br>
    /// Known as the "Windows" key on Windows,<br>
    /// the "Command" key on macOS,<br>
    /// and the "Super" or "Meta" key on Linux
    public boolean meta() {
        return (this.bitMask & InputConstants.MOD_SUPER) != 0;
    }

    /// Is Caps Lock currently active?
    public boolean capsLock() {
        return (this.bitMask & InputConstants.MOD_CAPS_LOCK) != 0;
    }

    /// Is Num Lock currently active?
    public boolean numLock() {
        return (this.bitMask & InputConstants.MOD_NUM_LOCK) != 0;
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
        InputConstants.KEY_LSHIFT,
        InputConstants.KEY_RSHIFT,
        InputConstants.KEY_LCONTROL,
        InputConstants.KEY_RCONTROL,
        InputConstants.KEY_LALT,
        InputConstants.KEY_RALT,
        InputConstants.KEY_LGUI,
        InputConstants.KEY_RGUI
    );
}
