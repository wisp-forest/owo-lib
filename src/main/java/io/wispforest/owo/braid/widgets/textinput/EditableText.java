package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Constrain;
import io.wispforest.owo.braid.widgets.basic.KeyboardInput;
import io.wispforest.owo.braid.widgets.basic.LayoutBuilder;
import io.wispforest.owo.braid.widgets.basic.VisitorWidget;
import io.wispforest.owo.braid.widgets.scroll.ScrollController;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import net.minecraft.text.Style;

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

        // TODO(glisco): rewrite once cancellable callbacks are merged
        private boolean callbackScheduled = false;
        private boolean blink = false;

        private final ScrollController horizontalController = new ScrollController();
        private final ScrollController verticalController = new ScrollController();
        private TextInput.Instance inputInstance;
        private WidgetInstance<?> scrollableInstance;

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
                if (this.inputInstance != null && this.scrollableInstance != null) {
                    var cursorX = this.inputInstance.cursorPosition().x + 2;
                    if (cursorX > this.scrollableInstance.transform.width() + this.horizontalController.offset()) {
                        this.horizontalController.setOffset(cursorX - this.scrollableInstance.transform.width());
                    }

                    cursorX -= 2;
                    if (cursorX < this.horizontalController.offset()) {
                        this.horizontalController.setOffset(cursorX);
                    }

                    if (this.widget().allowMultipleLines) {
                        var cursorY = this.inputInstance.cursorPosition().y;
                        if (cursorY > this.scrollableInstance.transform.height() + this.verticalController.offset()) {
                            this.verticalController.setOffset(cursorY - this.scrollableInstance.transform.height());
                        }

                        cursorY -= this.inputInstance.host().client().textRenderer.fontHeight;
                        if (cursorY < this.verticalController.offset()) {
                            this.verticalController.setOffset(cursorY);
                        }
                    }
                }
            });

            this.restartBlinking();
        }

        private void restartBlinking() {
            this.blink = true;

            this.setState(() -> this.showCursor = true);

            if (!this.callbackScheduled) {
                this.scheduleDelayedCallback(CURSOR_BLINK_INTERVAL, this::blink);
                this.callbackScheduled = true;
            }
        }

        private void stopBlinking() {
            this.blink = false;
            this.setState(() -> this.showCursor = false);
        }

        private void blink() {
            if (!this.blink) {
                this.callbackScheduled = false;
                return;
            }

            this.setState(() -> this.showCursor = !this.showCursor);
            this.scheduleDelayedCallback(CURSOR_BLINK_INTERVAL, this::blink);
        }

        @Override
        public Widget build(BuildContext context) {
            return new KeyboardInput(
                widget -> widget
                    .focusGainedCallback(this::restartBlinking)
                    .focusLostCallback(this::stopBlinking),
                new LayoutBuilder((innerContext, constraints) -> {
                    return new InstanceLocator(
                        widgetInstance -> this.scrollableInstance = widgetInstance,
                        new Scrollable(
                            true, true,
                            this.horizontalController,
                            this.verticalController,
                            new Constrain(
                                Constraints.of(constraints.minWidth(), constraints.minHeight(), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
                                new InstanceLocator(
                                    widgetInstance -> this.inputInstance = (TextInput.Instance) widgetInstance,
                                    new TextInput(
                                        this.widget().controller,
                                        this.showCursor,
                                        this.widget().softWrap,
                                        this.widget().autoFocus,
                                        this.widget().allowMultipleLines,
                                        this.widget().baseStyle
                                    )
                                )
                            )
                        )
                    );
                })
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
        return new VisitorWidget.Proxy<>(this, VISITOR);
    }
}