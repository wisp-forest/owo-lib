package io.wispforest.owo.braid.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NativeModifiersTest {

    @Test
    void nativeEventMasksRecognizeEitherSideWithoutConfusingLocksWithHeldModifiers() {
        // SDL3 distinguishes left/right modifiers; Caps/Num Lock have separate bits.
        var left = new KeyModifiers(0x0001 | 0x0040 | 0x0100 | 0x0400);
        var right = new KeyModifiers(0x0002 | 0x0080 | 0x0200 | 0x0800);
        for (var modifiers : new KeyModifiers[]{left, right}) {
            assertTrue(modifiers.shift());
            assertTrue(modifiers.ctrl());
            assertTrue(modifiers.alt());
            assertTrue(modifiers.meta());
            assertFalse(modifiers.capsLock());
            assertFalse(modifiers.numLock());
        }
        var locks = new KeyModifiers(0x1000 | 0x2000);
        assertTrue(locks.numLock());
        assertTrue(locks.capsLock());
        assertFalse(locks.shift());
        assertFalse(locks.ctrl());
        assertFalse(locks.alt());
        assertFalse(locks.meta());
    }

    @Test
    void pollingRecognizesLeftAndRightGuiKeysIndependently() {
        // Native SDL scancodes: left GUI=227, right GUI=231; ordinary A=4.
        for (int held : new int[]{227, 231}) {
            var binding = new EventBinding() {
                @Override
                public boolean isKeyPressed(int keyCode) {
                    return keyCode == held;
                }
            };
            assertTrue(binding.activeModifiers().meta(), "GUI key " + held);
            assertFalse(binding.activeModifiers().ctrl());
            assertTrue(KeyModifiers.isModifier(held));
        }
        assertFalse(KeyModifiers.isModifier(4));
        assertFalse(new EventBinding.Headless().activeModifiers().meta());
    }
}
