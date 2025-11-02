package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.Aabb2d;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.Builder;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import io.wispforest.owo.braid.widgets.scroll.ScrollAnimationSettings;
import io.wispforest.owo.braid.widgets.scroll.ScrollController;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class EditableText extends StatefulWidget {

    public final TextEditingController controller;
    protected boolean softWrap = true;
    protected boolean autoFocus = false;
    protected List<TextInput.Formatter> formatters = new ArrayList<>();
    protected Style baseStyle = Style.EMPTY;
    protected Text suggestion = Text.empty();
    protected boolean textShadow = false;
    protected boolean suggestionIsPlaceholder = false;

    public EditableText(
        TextEditingController controller,
        WidgetSetupCallback<EditableText> setupCallback
    ) {
        this.controller = controller;
        setupCallback.setup(this);
    }

    public EditableText softWrap(boolean softWrap) {
        this.assertMutable();
        this.softWrap = softWrap;
        return this;
    }

    public boolean softWrap() {
        return this.softWrap;
    }

    public EditableText autoFocus(boolean autoFocus) {
        this.assertMutable();
        this.autoFocus = autoFocus;
        return this;
    }

    public boolean autoFocus() {
        return this.autoFocus;
    }

    public EditableText formatter(TextInput.Formatter formatter) {
        this.assertMutable();
        this.formatters.add(formatter);
        return this;
    }

    public EditableText formatters(List<TextInput.Formatter> formatters) {
        this.assertMutable();
        this.formatters = formatters;
        return this;
    }

    public List<TextInput.Formatter> formatters() {
        return this.formatters;
    }

    public EditableText baseStyle(Style baseStyle) {
        this.assertMutable();
        this.baseStyle = baseStyle;
        return this;
    }

    public Style baseStyle() {
        return this.baseStyle;
    }

    public EditableText suggestion(Text suggestion) {
        this.assertMutable();
        this.suggestion = suggestion;
        return this;
    }

    public Text suggestion() {
        return this.suggestion;
    }

    public EditableText placeholder(Text placeholder) {
        this.assertMutable();
        this.suggestionIsPlaceholder = true;
        return this.suggestion(placeholder);
    }

    public EditableText textShadow(boolean shadow) {
        this.assertMutable();
        this.textShadow = shadow;
        return this;
    }

    public boolean textShadow() {
        return this.textShadow;
    }

    public EditableText singleLine() {
        return this
            .softWrap(false)
            .formatter(PatternFormatter.NO_NEWLINES);
    }

    @Override
    public WidgetState<EditableText> createState() {
        return new State();
    }

    public static class State extends WidgetState<EditableText> {

        private final Runnable listener = this::listenerCallback;

        private static final Duration CURSOR_BLINK_INTERVAL = Duration.ofMillis(300);
        private boolean showCursor = false;
        private boolean focused = false;

        private long blinkCallbackId = -1;

        private final ScrollController horizontalController = new ScrollController(this);
        private final ScrollController verticalController = new ScrollController(this);
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
                    new Aabb2d(
                        cursorPos.x,
                        cursorPos.y - lineHeight,
                        2,
                        lineHeight
                    )
                );
            });

            if (this.focused) {
                this.restartBlinking();
            }
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
            var widget = this.widget();
            return new Focusable(
                focusable -> focusable
                    .autoFocus(widget.autoFocus)
                    .focusGainedCallback(() -> {
                        this.focused = true;
                        this.restartBlinking();
                    })
                    .focusLostCallback(() -> {
                        this.focused = false;
                        this.stopBlinking();
                    })
                    .keyDownCallback((keyCode, modifiers) -> {
                        return ((TextInput.Instance) this.inputContext.instance()).onKeyDown(keyCode, modifiers);
                    })
                    .charCallback((charCode, modifiers) -> {
                        return ((TextInput.Instance) this.inputContext.instance()).onChar(charCode);
                    }),
                new Scrollable(
                    true, true,
                    this.horizontalController,
                    this.verticalController,
                    ScrollAnimationSettings.NO_ANIMATION,
                    new Builder(inputContext -> {
                        this.inputContext = inputContext;
                        return new TextInput(
                            widget.controller,
                            this.showCursor,
                            widget.softWrap,
                            widget.formatters,
                            widget.baseStyle,
                            widget.textShadow,
                            !widget.suggestionIsPlaceholder || widget.controller.value().text().isEmpty()
                                ? widget.suggestion
                                : Text.empty()
                        );
                    })
                )
            );
        }
    }
}
