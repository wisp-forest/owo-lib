package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.KeyboardInput;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Style;

public class TextBox extends StatefulWidget {

    public final TextEditingController controller;
    public final boolean softWrap;
    public final boolean autoFocus;
    public final boolean allowMultipleLines;
    public final Style baseStyle;

    public TextBox(TextEditingController controller, boolean softWrap, boolean autoFocus, boolean allowMultipleLines, Style baseStyle) {
        this.controller = controller;
        this.softWrap = softWrap;
        this.autoFocus = autoFocus;
        this.allowMultipleLines = allowMultipleLines;
        this.baseStyle = baseStyle;
    }

    @Override
    public WidgetState<TextBox> createState() {
        return new State();
    }

    public static class State extends WidgetState<TextBox> {

        private boolean focused = false;

        @Override
        public Widget build(BuildContext context) {
            return new Box(
                this.focused ? Color.WHITE : Color.ofRgb(0x8f8f8f),
                new KeyboardInput(
                    widget -> widget
                        .focusGainedCallback(() -> this.setState(() -> this.focused = true))
                        .focusLostCallback(() -> this.setState(() -> this.focused = false)),
                    new Padding(
                        Insets.all(1),
                        new Box(
                            Color.BLACK,
                            new Padding(
                                Insets.all(2),
                                new EditableText(
                                    this.widget().controller,
                                    this.widget().softWrap,
                                    this.widget().autoFocus,
                                    this.widget().allowMultipleLines,
                                    this.widget().baseStyle
                                )
                            )
                        )
                    )
                )
            );
        }
    }
}
