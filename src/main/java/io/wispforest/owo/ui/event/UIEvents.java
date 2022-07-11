package io.wispforest.owo.ui.event;

import io.wispforest.owo.util.EventStream;

public class UIEvents {

    public static EventStream<MouseDown> newMouseDownStream() {
        return new EventStream<>(subscribers -> (mouseX, mouseY, button) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onMouseDown(mouseX, mouseY, button);
            }
            return anyTriggered;
        });
    }

    public static EventStream<MouseUp> newMouseUpStream() {
        return new EventStream<>(subscribers -> (mouseX, mouseY, button) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onMouseUp(mouseX, mouseY, button);
            }
            return anyTriggered;
        });
    }

    public static EventStream<MouseScroll> newMouseScrollStream() {
        return new EventStream<>(subscribers -> (mouseX, mouseY, amount) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onMouseScroll(mouseX, mouseY, amount);
            }
            return anyTriggered;
        });
    }

    public static EventStream<MouseDrag> newMouseDragStream() {
        return new EventStream<>(subscribers -> (mouseX, mouseY, deltaX, deltaY, button) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
            }
            return anyTriggered;
        });
    }

    public static EventStream<KeyPress> newKeyPressStream() {
        return new EventStream<>(subscribers -> (keyCode, scanCode, modifiers) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onKeyPress(keyCode, scanCode, modifiers);
            }
            return anyTriggered;
        });
    }

    public static EventStream<CharTyped> newCharTypedStream() {
        return new EventStream<>(subscribers -> (chr, modifiers) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onCharTyped(chr, modifiers);
            }
            return anyTriggered;
        });
    }

    public static EventStream<FocusGained> newFocusGainedStream() {
        return new EventStream<>(subscribers -> source -> {
            for (var subscriber : subscribers) {
                subscriber.onFocusGained(source);
            }
        });
    }

    public static EventStream<FocusLost> newFocusLostStream() {
        return new EventStream<>(subscribers -> () -> {
            for (var subscriber : subscribers) {
                subscriber.onFocusLost();
            }
        });
    }

    public static EventStream<MouseEnter> newMouseEnterStream() {
        return new EventStream<>(subscribers -> () -> {
            for (var subscriber : subscribers) {
                subscriber.onMouseEnter();
            }
        });
    }

    public static EventStream<MouseLeave> newMouseLeaveStream() {
        return new EventStream<>(subscribers -> () -> {
            for (var subscriber : subscribers) {
                subscriber.onMouseLeave();
            }
        });
    }
}
