package io.wispforest.owo.extras;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ScreenEventFactory {
    public static Event<ScreenEvents.Remove> createRemoveEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.Remove.class, callbacks -> screen -> {
            for (ScreenEvents.Remove callback : callbacks) {
                callback.onRemove(screen);
            }
        });
    }

    public static Event<ScreenEvents.BeforeRender> createBeforeRenderEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.BeforeRender.class, callbacks -> (screen, matrices, mouseX, mouseY, tickDelta) -> {
            for (ScreenEvents.BeforeRender callback : callbacks) {
                callback.beforeRender(screen, matrices, mouseX, mouseY, tickDelta);
            }
        });
    }

    public static Event<ScreenEvents.AfterRender> createAfterRenderEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AfterRender.class, callbacks -> (screen, matrices, mouseX, mouseY, tickDelta) -> {
            for (ScreenEvents.AfterRender callback : callbacks) {
                callback.afterRender(screen, matrices, mouseX, mouseY, tickDelta);
            }
        });
    }

    public static Event<ScreenEvents.BeforeTick> createBeforeTickEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.BeforeTick.class, callbacks -> screen -> {
            for (ScreenEvents.BeforeTick callback : callbacks) {
                callback.beforeTick(screen);
            }
        });
    }

    public static Event<ScreenEvents.AfterTick> createAfterTickEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AfterTick.class, callbacks -> screen -> {
            for (ScreenEvents.AfterTick callback : callbacks) {
                callback.afterTick(screen);
            }
        });
    }

    // Keyboard events

    public static Event<ScreenEvents.AllowKeyPress> createAllowKeyPressEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AllowKeyPress.class, callbacks -> (screen, key, scancode, modifiers) -> {
            for (ScreenEvents.AllowKeyPress callback : callbacks) {
                if (!callback.allowKeyPress(screen, key, scancode, modifiers)) {
                    return false;
                }
            }

            return true;
        });
    }

    public static Event<ScreenEvents.BeforeKeyPress> createBeforeKeyPressEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.BeforeKeyPress.class, callbacks -> (screen, key, scancode, modifiers) -> {
            for (ScreenEvents.BeforeKeyPress callback : callbacks) {
                callback.beforeKeyPress(screen, key, scancode, modifiers);
            }
        });
    }

    public static Event<ScreenEvents.AfterKeyPress> createAfterKeyPressEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AfterKeyPress.class, callbacks -> (screen, key, scancode, modifiers) -> {
            for (ScreenEvents.AfterKeyPress callback : callbacks) {
                callback.afterKeyPress(screen, key, scancode, modifiers);
            }
        });
    }

    public static Event<ScreenEvents.AllowKeyRelease> createAllowKeyReleaseEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AllowKeyRelease.class, callbacks -> (screen, key, scancode, modifiers) -> {
            for (ScreenEvents.AllowKeyRelease callback : callbacks) {
                if (!callback.allowKeyRelease(screen, key, scancode, modifiers)) {
                    return false;
                }
            }

            return true;
        });
    }

    public static Event<ScreenEvents.BeforeKeyRelease> createBeforeKeyReleaseEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.BeforeKeyRelease.class, callbacks -> (screen, key, scancode, modifiers) -> {
            for (ScreenEvents.BeforeKeyRelease callback : callbacks) {
                callback.beforeKeyRelease(screen, key, scancode, modifiers);
            }
        });
    }

    public static Event<ScreenEvents.AfterKeyRelease> createAfterKeyReleaseEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AfterKeyRelease.class, callbacks -> (screen, key, scancode, modifiers) -> {
            for (ScreenEvents.AfterKeyRelease callback : callbacks) {
                callback.afterKeyRelease(screen, key, scancode, modifiers);
            }
        });
    }

    // Mouse Events

    public static Event<ScreenEvents.AllowMouseClick> createAllowMouseClickEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AllowMouseClick.class, callbacks -> (screen, mouseX, mouseY, button) -> {
            for (ScreenEvents.AllowMouseClick callback : callbacks) {
                if (!callback.allowMouseClick(screen, mouseX, mouseY, button)) {
                    return false;
                }
            }

            return true;
        });
    }

    public static Event<ScreenEvents.BeforeMouseClick> createBeforeMouseClickEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.BeforeMouseClick.class, callbacks -> (screen, mouseX, mouseY, button) -> {
            for (ScreenEvents.BeforeMouseClick callback : callbacks) {
                callback.beforeMouseClick(screen, mouseX, mouseY, button);
            }
        });
    }

    public static Event<ScreenEvents.AfterMouseClick> createAfterMouseClickEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AfterMouseClick.class, callbacks -> (screen, mouseX, mouseY, button) -> {
            for (ScreenEvents.AfterMouseClick callback : callbacks) {
                callback.afterMouseClick(screen, mouseX, mouseY, button);
            }
        });
    }

    public static Event<ScreenEvents.AllowMouseRelease> createAllowMouseReleaseEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AllowMouseRelease.class, callbacks -> (screen, mouseX, mouseY, button) -> {
            for (ScreenEvents.AllowMouseRelease callback : callbacks) {
                if (!callback.allowMouseRelease(screen, mouseX, mouseY, button)) {
                    return false;
                }
            }

            return true;
        });
    }

    public static Event<ScreenEvents.BeforeMouseRelease> createBeforeMouseReleaseEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.BeforeMouseRelease.class, callbacks -> (screen, mouseX, mouseY, button) -> {
            for (ScreenEvents.BeforeMouseRelease callback : callbacks) {
                callback.beforeMouseRelease(screen, mouseX, mouseY, button);
            }
        });
    }

    public static Event<ScreenEvents.AfterMouseRelease> createAfterMouseReleaseEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AfterMouseRelease.class, callbacks -> (screen, mouseX, mouseY, button) -> {
            for (ScreenEvents.AfterMouseRelease callback : callbacks) {
                callback.afterMouseRelease(screen, mouseX, mouseY, button);
            }
        });
    }

    public static Event<ScreenEvents.AllowMouseScroll> createAllowMouseScrollEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AllowMouseScroll.class, callbacks -> (screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
            for (ScreenEvents.AllowMouseScroll callback : callbacks) {
                if (!callback.allowMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount)) {
                    return false;
                }
            }

            return true;
        });
    }

    public static Event<ScreenEvents.BeforeMouseScroll> createBeforeMouseScrollEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.BeforeMouseScroll.class, callbacks -> (screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
            for (ScreenEvents.BeforeMouseScroll callback : callbacks) {
                callback.beforeMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
            }
        });
    }

    public static Event<ScreenEvents.AfterMouseScroll> createAfterMouseScrollEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AfterMouseScroll.class, callbacks -> (screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
            for (ScreenEvents.AfterMouseScroll callback : callbacks) {
                callback.afterMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
            }
        });
    }

    private ScreenEventFactory() {}
}
