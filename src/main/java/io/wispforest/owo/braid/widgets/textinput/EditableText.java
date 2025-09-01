package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.scroll.ScrollController;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.time.Duration;

public class EditableText extends StatefulWidget {

    public final TextEditingController controller;
    private boolean softWrap = true;
    private boolean autoFocus = false;
    private int maxLines = -1;
    private int maxCharacters = -1;
    private Style baseStyle = Style.EMPTY;
    private Text suggestion = Text.empty();
    private boolean textShadow = false;
    private boolean suggestionIsPlaceholder = false;

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

    public EditableText maxLines(int maxLines) {
        this.assertMutable();
        this.maxLines = maxLines;
        return this;
    }

    public int maxLines() {
        return this.maxLines;
    }

    public EditableText maxCharacters(int maxCharacters) {
        this.assertMutable();
        this.maxCharacters = maxCharacters;
        return this;
    }

    public int maxCharacters() {
        return this.maxCharacters;
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
        return this.suggestion(controller.text.isEmpty() ? placeholder : Text.empty());
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
            .maxLines(1);
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
            return new KeyboardInput(
                widget -> widget
                    .focusGainedCallback(() -> {
                        this.focused = true;
                        this.restartBlinking();
                    })
                    .focusLostCallback(() -> {
                        this.focused = false;
                        this.stopBlinking();
                    }),
                new Scrollable(
                    true, this.widget().maxLines != 1,
                    this.horizontalController,
                    this.verticalController,
                    new Builder(inputContext -> {
                        this.inputContext = inputContext;
                        return new TextInput(
                            this.widget().controller,
                            this.showCursor,
                            this.widget().softWrap,
                            this.widget().autoFocus,
                            this.widget().maxLines,
                            this.widget().maxCharacters,
                            this.widget().baseStyle,
                            this.widget().textShadow,
                            this.widget().suggestionIsPlaceholder
                                ? this.widget().controller.text.isEmpty()
                                    ? this.widget().suggestion
                                    : Text.empty()
                                : this.widget().suggestion
                        );
                    })
                )
            );
        }
    }
}
