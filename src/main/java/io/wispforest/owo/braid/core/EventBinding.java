package io.wispforest.owo.braid.core;

import com.mojang.blaze3d.platform.InputConstants;
import io.wispforest.owo.braid.core.events.UserEvent;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public abstract class EventBinding {

    private final List<EventSlot> bufferedEvents = new ArrayList<>();

    public EventSlot add(UserEvent event) {
        var slot = new EventSlot(event);
        this.bufferedEvents.add(slot);

        return slot;
    }

    List<EventSlot> poll() {
        var events = new ArrayList<>(this.bufferedEvents);
        this.bufferedEvents.clear();

        return events;
    }

    public abstract boolean isKeyPressed(int keyCode);

    public KeyModifiers activeModifiers() {
        return new KeyModifiers(
            (this.isKeyPressed(InputConstants.KEY_LSHIFT) || this.isKeyPressed(InputConstants.KEY_RSHIFT) ? InputConstants.MOD_SHIFT : 0)
            | (this.isKeyPressed(InputConstants.KEY_LCONTROL) || this.isKeyPressed(InputConstants.KEY_RCONTROL) ? InputConstants.MOD_CONTROL : 0)
            | (this.isKeyPressed(InputConstants.KEY_LALT) || this.isKeyPressed(InputConstants.KEY_RALT) ? InputConstants.MOD_ALT : 0)
            | (this.isKeyPressed(InputConstants.KEY_LGUI) || this.isKeyPressed(InputConstants.KEY_RGUI) ? InputConstants.MOD_SUPER : 0)
            | (this.isKeyPressed(InputConstants.KEY_NUMLOCK) ? InputConstants.MOD_NUM_LOCK : 0)
            | (this.isKeyPressed(InputConstants.KEY_CAPSLOCK) ? InputConstants.MOD_CAPS_LOCK : 0)
        );
    }

    public static class EventSlot {
        final UserEvent event;
        private boolean handled = false;

        public EventSlot(UserEvent event) {
            this.event = event;
        }

        public boolean handled() {
            return this.handled;
        }

        void markHandled() {
            this.handled = true;
        }
    }

    // ---

    public static class Headless extends EventBinding {
        @Override
        public boolean isKeyPressed(int keyCode) {
            return false;
        }
    }

    public static class Default extends EventBinding {
        @Override
        public boolean isKeyPressed(int keyCode) {
            return InputConstants.isKeyDown(keyCode);
        }
    }
}
