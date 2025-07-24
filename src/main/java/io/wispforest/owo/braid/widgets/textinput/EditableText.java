package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Builder;
import io.wispforest.owo.braid.widgets.basic.KeyboardInput;
import io.wispforest.owo.braid.widgets.basic.VisitorWidget;
import io.wispforest.owo.braid.widgets.scroll.ScrollController;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import net.minecraft.text.Style;
import net.minecraft.util.math.Box;

import java.time.Duration;
import java.util.function.Consumer;

public class EditableText extends StatefulWidget {

    public final TextEditingController controller;
    public final boolean softWrap;
    public final boolean autoFocus;
    public final boolean allowMultipleLines;
    public final Style baseStyle;

    public EditableText(TextEditingController controller, boolean softWrap, boolean autoFocus, boolean allowMultipleLines, Style baseStyle) {
        this.controller = controller;
        this.softWrap = softWrap;
        this.autoFocus = autoFocus;
        this.allowMultipleLines = allowMultipleLines;
        this.baseStyle = baseStyle;
    }

    @Override
    public WidgetState<EditableText> createState() {
        return new State();
    }

    public static class State extends WidgetState<EditableText> {

        private final Runnable listener = this::listenerCallback;

        private static final Duration CURSOR_BLINK_INTERVAL = Duration.ofMillis(300);
        private boolean showCursor = false;

        private long blinkCallbackId = -1;

        private final ScrollController horizontalController = new ScrollController();
        private final ScrollController verticalController = new ScrollController();
        private BuildContext inputContext;

        @Override
        public void init() {
            this.widget().controller.addListener(this.listener);
        }

        @Override
        public void didUpdateWidget(EditableText oldWidget) {
            if (this.widget().controller != oldWidget.controller) {
                oldWidget.controller.removeListener(this.listener);
                this.widget().controller.addListener(this.listener);
            }
        }

        @Override
        public void dispose() {
            this.widget().controller.removeListener(this.listener);
        }

        private void listenerCallback() {
            this.schedulePostLayoutCallback(() -> {
                var inputInstance = (TextInput.Instance) this.inputContext.instance();
                var cursorPos = inputInstance.cursorPosition();
                var lineHeight = inputInstance.host().client().textRenderer.fontHeight;

                Scrollable.revealAabb(
                    this.inputContext,
                    new Box(
                        cursorPos.x,
                        cursorPos.y - lineHeight,
                        0,
                        cursorPos.x + 2,
                        cursorPos.y,
                        0
                    )
                );
            });

            this.restartBlinking();
        }

        private void restartBlinking() {
            if (this.blinkCallbackId != -1) {
                this.cancelDelayedCallback(this.blinkCallbackId);
                this.blinkCallbackId = -1;
            }

            this.setState(() -> this.showCursor = true);

            this.blinkCallbackId = this.scheduleDelayedCallback(CURSOR_BLINK_INTERVAL, this::blink);
        }

        private void stopBlinking() {
            if (this.blinkCallbackId != -1) {
                this.cancelDelayedCallback(this.blinkCallbackId);
                this.blinkCallbackId = -1;
            }

            this.setState(() -> this.showCursor = false);
        }

        private void blink() {
            this.setState(() -> this.showCursor = !this.showCursor);
            this.blinkCallbackId = this.scheduleDelayedCallback(CURSOR_BLINK_INTERVAL, this::blink);
        }

        @Override
        public Widget build(BuildContext context) {
            return new KeyboardInput(
                widget -> widget
                    .focusGainedCallback(this::restartBlinking)
                    .focusLostCallback(this::stopBlinking),
                new Scrollable(
                    true, true,
                    this.horizontalController,
                    this.verticalController,
                    new Builder(inputContext -> {
                        this.inputContext = inputContext;
                        return new TextInput(
                            this.widget().controller,
                            this.showCursor,
                            this.widget().softWrap,
                            this.widget().autoFocus,
                            this.widget().allowMultipleLines,
                            this.widget().baseStyle
                        );
                    })
                )
            );
        }
    }
}

class InstanceLocator extends VisitorWidget {

    public final Consumer<WidgetInstance<?>> callback;

    public InstanceLocator(Consumer<WidgetInstance<?>> callback, Widget child) {
        super(child);
        this.callback = callback;
    }

    public static final Visitor<InstanceLocator> VISITOR = (widget, instance) -> {
        widget.callback.accept(instance);
    };

    @Override
    public Proxy<?> proxy() {
        return new Proxy<>(this, VISITOR);
    }
}