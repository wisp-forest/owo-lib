package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
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

        private final Runnable listener = () -> this.setState(() -> {});

        @Override
        public void init() {
            this.widget().controller.addListener(this.listener);
        }

        @Override
        public void didUpdateWidget(TextBox oldWidget) {
            super.didUpdateWidget(oldWidget);

            if (this.widget().controller != oldWidget.controller) {
                oldWidget.controller.removeListener(this.listener);
                this.widget().controller.addListener(this.listener);
            }
        }

        @Override
        public Widget build(BuildContext context) {
            return new Box(
                this.widget().controller.focused() ? Color.WHITE : Color.ofRgb(0x8f8f8f),
                new Padding(
                    Insets.all(1),
                    new Box(
                        Color.BLACK,
                        new Padding(
                            Insets.all(2),
                            new TextInput(
                                this.widget().controller,
                                this.widget().softWrap,
                                this.widget().allowMultipleLines
                            )
                        )
                    )
                )
            );
        }
    }
}
