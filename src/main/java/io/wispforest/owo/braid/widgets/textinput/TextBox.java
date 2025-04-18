package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.ListenableBuilder;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.ui.core.Color;

public class TextBox extends StatelessWidget {

    public final TextEditingController controller;
    public final boolean allowMultipleLines;
    public final boolean softWrap;

    public TextBox(TextEditingController controller, boolean allowMultipleLines, boolean softWrap) {
        this.controller = controller;
        this.allowMultipleLines = allowMultipleLines;
        this.softWrap = softWrap;
    }

    @Override
    public Widget build(BuildContext context) {
        return new ListenableBuilder(
            this.controller,
            widget -> new Box(
                this.controller.focused() ? Color.WHITE : Color.ofRgb(0x8f8f8f),
                widget
            ),
            new Padding(
                Insets.all(1),
                new Box(
                    Color.BLACK,
                    new Padding(
                        Insets.all(2),
                        new TextInput(
                            this.controller,
                            this.softWrap,
                            this.allowMultipleLines
                        )
                    )
                )
            )
        );
    }
}
