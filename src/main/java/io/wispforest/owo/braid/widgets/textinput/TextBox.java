package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.KeyboardInput;
import io.wispforest.owo.braid.widgets.basic.ListenableBuilder;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.ui.core.Color;

import java.time.Duration;

public class TextBox extends StatefulWidget {

    public final TextEditingController controller;
    public final boolean allowMultipleLines;
    public final boolean softWrap;

    public TextBox(TextEditingController controller, boolean allowMultipleLines, boolean softWrap) {
        this.controller = controller;
        this.allowMultipleLines = allowMultipleLines;
        this.softWrap = softWrap;
    }

    @Override
    public WidgetState<TextBox> createState() {
        return new State();
    }

    public static class State extends WidgetState<TextBox> {

        private boolean focused = false;
        private boolean showCursor = false;

        protected void updateCursor() {
            if (!this.focused) return;

            this.setState(() -> this.showCursor = !this.showCursor);
            this.scheduleDelayedCallback(Duration.ofMillis(500), this::updateCursor);
        }

        @Override
        public Widget build(BuildContext context) {
            return new Box(
                this.focused ? Color.WHITE : Color.ofRgb(0x8f8f8f),
                new KeyboardInput(
                    widget -> widget
                        .focusGainedCallback(() -> {
                            this.setState(() -> this.focused = true);
                            this.updateCursor();
                        })
                        .focusLostCallback(() -> {
                            this.setState(() -> {
                                this.focused = false;
                                this.showCursor = false;
                            });
                        }),
                    new Padding(
                        Insets.all(1),
                        new Box(
                            Color.BLACK,
                            new Padding(
                                Insets.all(2),
                                new ListenableBuilder(
                                    this.widget().controller,
                                    innerContext -> new TextInput(
                                        this.widget().controller,
                                        this.showCursor,
                                        this.widget().softWrap,
                                        this.widget().allowMultipleLines
                                    )
                                )
                            )
                        )
                    )
                )
            );
        }
    }
}
