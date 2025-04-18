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

        @Override
        public Widget build(BuildContext context) {
            return new ListenableBuilder(
                this.widget().controller,
                buildContext -> new Box(
                    this.focused ? Color.WHITE : Color.ofRgb(0x8f8f8f),
                    new KeyboardInput(
                        widget -> widget
                            .focusGainedCallback(() -> setState(() -> this.focused = true))
                            .focusLostCallback(() -> setState(() -> this.focused = false)),
                        new Padding(
                            Insets.all(1),
                            new Box(
                                Color.BLACK,
                                new Padding(
                                    Insets.all(2),
                                    new TextInput(
                                        this.widget().controller,
                                        this.focused,
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
