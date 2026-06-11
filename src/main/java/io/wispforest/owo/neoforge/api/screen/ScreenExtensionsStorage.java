package io.wispforest.owo.neoforge.api.screen;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

record ScreenExtensionsStorage(
    Event<ScreenEvents.Remove> fabric_getRemoveEvent,
    Event<ScreenEvents.BeforeExtract> fabric_getBeforeRenderEvent,
    Event<ScreenEvents.AfterBackground> fabric_getAfterBackgroundEvent,
    Event<ScreenEvents.AfterExtract> fabric_getAfterRenderEvent,
    Event<ScreenEvents.BeforeTick> fabric_getBeforeTickEvent,
    Event<ScreenEvents.AfterTick> fabric_getAfterTickEvent,
    // Keyboard
    Event<ScreenKeyboardEvents.AllowKeyPress> fabric_getAllowKeyPressEvent,
    Event<ScreenKeyboardEvents.BeforeKeyPress> fabric_getBeforeKeyPressEvent,
    Event<ScreenKeyboardEvents.AfterKeyPress> fabric_getAfterKeyPressEvent,
    Event<ScreenKeyboardEvents.AllowKeyRelease> fabric_getAllowKeyReleaseEvent,
    Event<ScreenKeyboardEvents.BeforeKeyRelease> fabric_getBeforeKeyReleaseEvent,
    Event<ScreenKeyboardEvents.AfterKeyRelease> fabric_getAfterKeyReleaseEvent,
    // Mouse
    Event<ScreenMouseEvents.AllowMouseClick> fabric_getAllowMouseClickEvent,
    Event<ScreenMouseEvents.BeforeMouseClick> fabric_getBeforeMouseClickEvent,
    Event<ScreenMouseEvents.AfterMouseClick> fabric_getAfterMouseClickEvent,
    Event<ScreenMouseEvents.AllowMouseRelease> fabric_getAllowMouseReleaseEvent,
    Event<ScreenMouseEvents.BeforeMouseRelease> fabric_getBeforeMouseReleaseEvent,
    Event<ScreenMouseEvents.AfterMouseRelease> fabric_getAfterMouseReleaseEvent,
    Event<ScreenMouseEvents.AllowMouseDrag> fabric_getAllowMouseDragEvent,
    Event<ScreenMouseEvents.BeforeMouseDrag> fabric_getBeforeMouseDragEvent,
    Event<ScreenMouseEvents.AfterMouseDrag> fabric_getAfterMouseDragEvent,
    Event<ScreenMouseEvents.AllowMouseScroll> fabric_getAllowMouseScrollEvent,
    Event<ScreenMouseEvents.BeforeMouseScroll> fabric_getBeforeMouseScrollEvent,
    Event<ScreenMouseEvents.AfterMouseScroll> fabric_getAfterMouseScrollEvent
) {
    ScreenExtensionsStorage() {
        this(
            ScreenEventFactory.createRemoveEvent(),
            ScreenEventFactory.createBeforeRenderEvent(),
            ScreenEventFactory.createAfterBackgroundEvent(),
            ScreenEventFactory.createAfterRenderEvent(),
            ScreenEventFactory.createBeforeTickEvent(),
            ScreenEventFactory.createAfterTickEvent(),
            // Keyboard
            ScreenEventFactory.createAllowKeyPressEvent(),
            ScreenEventFactory.createBeforeKeyPressEvent(),
            ScreenEventFactory.createAfterKeyPressEvent(),
            ScreenEventFactory.createAllowKeyReleaseEvent(),
            ScreenEventFactory.createBeforeKeyReleaseEvent(),
            ScreenEventFactory.createAfterKeyReleaseEvent(),
            // Mouse
            ScreenEventFactory.createAllowMouseClickEvent(),
            ScreenEventFactory.createBeforeMouseClickEvent(),
            ScreenEventFactory.createAfterMouseClickEvent(),
            ScreenEventFactory.createAllowMouseReleaseEvent(),
            ScreenEventFactory.createBeforeMouseReleaseEvent(),
            ScreenEventFactory.createAfterMouseReleaseEvent(),
            ScreenEventFactory.createAllowMouseDragEvent(),
            ScreenEventFactory.createBeforeMouseDragEvent(),
            ScreenEventFactory.createAfterMouseDragEvent(),
            ScreenEventFactory.createAllowMouseScrollEvent(),
            ScreenEventFactory.createBeforeMouseScrollEvent(),
            ScreenEventFactory.createAfterMouseScrollEvent()
        );
    }
}

class ScreenEventFactory {
    public static Event<ScreenEvents.Remove> createRemoveEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.Remove.class, callbacks -> screen -> {
            for (ScreenEvents.Remove callback : callbacks) {
                callback.onRemove(screen);
            }
        });
    }

    public static Event<ScreenEvents.BeforeExtract> createBeforeRenderEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.BeforeExtract.class, callbacks -> (screen, matrices, mouseX, mouseY, tickDelta) -> {
            for (ScreenEvents.BeforeExtract callback : callbacks) {
                callback.beforeExtract(screen, matrices, mouseX, mouseY, tickDelta);
            }
        });
    }

    public static Event<ScreenEvents.AfterBackground> createAfterBackgroundEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AfterBackground.class, callbacks -> (screen, matrices, mouseX, mouseY, tickDelta) -> {
            for (ScreenEvents.AfterBackground callback : callbacks) {
                callback.afterBackground(screen, matrices, mouseX, mouseY, tickDelta);
            }
        });
    }

    public static Event<ScreenEvents.AfterExtract> createAfterRenderEvent() {
        return EventFactory.createArrayBacked(ScreenEvents.AfterExtract.class, callbacks -> (screen, matrices, mouseX, mouseY, tickDelta) -> {
            for (ScreenEvents.AfterExtract callback : callbacks) {
                callback.afterExtract(screen, matrices, mouseX, mouseY, tickDelta);
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

    public static Event<ScreenKeyboardEvents.AllowKeyPress> createAllowKeyPressEvent() {
        return EventFactory.createArrayBacked(ScreenKeyboardEvents.AllowKeyPress.class, callbacks -> (screen, context) -> {
            for (ScreenKeyboardEvents.AllowKeyPress callback : callbacks) {
                if (!callback.allowKeyPress(screen, context)) {
                    return false;
                }
            }

            return true;
        });
    }

    public static Event<ScreenKeyboardEvents.BeforeKeyPress> createBeforeKeyPressEvent() {
        return EventFactory.createArrayBacked(ScreenKeyboardEvents.BeforeKeyPress.class, callbacks -> (screen, context) -> {
            for (ScreenKeyboardEvents.BeforeKeyPress callback : callbacks) {
                callback.beforeKeyPress(screen, context);
            }
        });
    }

    public static Event<ScreenKeyboardEvents.AfterKeyPress> createAfterKeyPressEvent() {
        return EventFactory.createArrayBacked(ScreenKeyboardEvents.AfterKeyPress.class, callbacks -> (screen, context) -> {
            for (ScreenKeyboardEvents.AfterKeyPress callback : callbacks) {
                callback.afterKeyPress(screen, context);
            }
        });
    }

    public static Event<ScreenKeyboardEvents.AllowKeyRelease> createAllowKeyReleaseEvent() {
        return EventFactory.createArrayBacked(ScreenKeyboardEvents.AllowKeyRelease.class, callbacks -> (screen, context) -> {
            for (ScreenKeyboardEvents.AllowKeyRelease callback : callbacks) {
                if (!callback.allowKeyRelease(screen, context)) {
                    return false;
                }
            }

            return true;
        });
    }

    public static Event<ScreenKeyboardEvents.BeforeKeyRelease> createBeforeKeyReleaseEvent() {
        return EventFactory.createArrayBacked(ScreenKeyboardEvents.BeforeKeyRelease.class, callbacks -> (screen, context) -> {
            for (ScreenKeyboardEvents.BeforeKeyRelease callback : callbacks) {
                callback.beforeKeyRelease(screen, context);
            }
        });
    }

    public static Event<ScreenKeyboardEvents.AfterKeyRelease> createAfterKeyReleaseEvent() {
        return EventFactory.createArrayBacked(ScreenKeyboardEvents.AfterKeyRelease.class, callbacks -> (screen, context) -> {
            for (ScreenKeyboardEvents.AfterKeyRelease callback : callbacks) {
                callback.afterKeyRelease(screen, context);
            }
        });
    }

    // Mouse Events

    public static Event<ScreenMouseEvents.AllowMouseClick> createAllowMouseClickEvent() {
        return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseClick.class, callbacks -> (screen, context) -> {
            for (ScreenMouseEvents.AllowMouseClick callback : callbacks) {
                if (!callback.allowMouseClick(screen, context)) {
                    return false;
                }
            }

            return true;
        });
    }

    public static Event<ScreenMouseEvents.BeforeMouseClick> createBeforeMouseClickEvent() {
        return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseClick.class, callbacks -> (screen, context) -> {
            for (ScreenMouseEvents.BeforeMouseClick callback : callbacks) {
                callback.beforeMouseClick(screen, context);
            }
        });
    }

    public static Event<ScreenMouseEvents.AfterMouseClick> createAfterMouseClickEvent() {
        return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseClick.class, callbacks -> (screen, context, consumed) -> {
            boolean consume = false;

            for (ScreenMouseEvents.AfterMouseClick callback : callbacks) {
                consume |= callback.afterMouseClick(screen, context, consume | consumed);
            }

            return consume;
        });
    }

    public static Event<ScreenMouseEvents.AllowMouseRelease> createAllowMouseReleaseEvent() {
        return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseRelease.class, callbacks -> (screen, context) -> {
            for (ScreenMouseEvents.AllowMouseRelease callback : callbacks) {
                if (!callback.allowMouseRelease(screen, context)) {
                    return false;
                }
            }

            return true;
        });
    }

    public static Event<ScreenMouseEvents.BeforeMouseRelease> createBeforeMouseReleaseEvent() {
        return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseRelease.class, callbacks -> (screen, context) -> {
            for (ScreenMouseEvents.BeforeMouseRelease callback : callbacks) {
                callback.beforeMouseRelease(screen, context);
            }
        });
    }

    public static Event<ScreenMouseEvents.AfterMouseRelease> createAfterMouseReleaseEvent() {
        return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseRelease.class, callbacks -> (screen, context, consumed) -> {
            boolean consume = false;

            for (ScreenMouseEvents.AfterMouseRelease callback : callbacks) {
                consume |= callback.afterMouseRelease(screen, context, consume | consumed);
            }

            return consume;
        });
    }

    public static Event<ScreenMouseEvents.AllowMouseDrag> createAllowMouseDragEvent() {
        return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseDrag.class, callbacks -> (screen, context, horizontalAmount, verticalAmount) -> {
            for (ScreenMouseEvents.AllowMouseDrag callback : callbacks) {
                if (!callback.allowMouseDrag(screen, context, horizontalAmount, verticalAmount)) {
                    return false;
                }
            }

            return true;
        });
    }

    public static Event<ScreenMouseEvents.BeforeMouseDrag> createBeforeMouseDragEvent() {
        return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseDrag.class, callbacks -> (screen, context, horizontalAmount, verticalAmount) -> {
            for (ScreenMouseEvents.BeforeMouseDrag callback : callbacks) {
                callback.beforeMouseDrag(screen, context, horizontalAmount, verticalAmount);
            }
        });
    }

    public static Event<ScreenMouseEvents.AfterMouseDrag> createAfterMouseDragEvent() {
        return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseDrag.class, callbacks -> (screen, context, horizontalAmount, verticalAmount, consumed) -> {
            boolean consume = false;

            for (ScreenMouseEvents.AfterMouseDrag callback : callbacks) {
                consume |= callback.afterMouseDrag(screen, context, horizontalAmount, verticalAmount, consume | consumed);
            }

            return consume;
        });
    }

    public static Event<ScreenMouseEvents.AllowMouseScroll> createAllowMouseScrollEvent() {
        return EventFactory.createArrayBacked(ScreenMouseEvents.AllowMouseScroll.class, callbacks -> (screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
            for (ScreenMouseEvents.AllowMouseScroll callback : callbacks) {
                if (!callback.allowMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount)) {
                    return false;
                }
            }

            return true;
        });
    }

    public static Event<ScreenMouseEvents.BeforeMouseScroll> createBeforeMouseScrollEvent() {
        return EventFactory.createArrayBacked(ScreenMouseEvents.BeforeMouseScroll.class, callbacks -> (screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
            for (ScreenMouseEvents.BeforeMouseScroll callback : callbacks) {
                callback.beforeMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
            }
        });
    }

    public static Event<ScreenMouseEvents.AfterMouseScroll> createAfterMouseScrollEvent() {
        return EventFactory.createArrayBacked(ScreenMouseEvents.AfterMouseScroll.class, callbacks -> (screen, mouseX, mouseY, horizontalAmount, verticalAmount, consumed) -> {
            boolean consume = false;

            for (ScreenMouseEvents.AfterMouseScroll callback : callbacks) {
                consume |= callback.afterMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount, consume | consumed);
            }

            return consume;
        });
    }
}
