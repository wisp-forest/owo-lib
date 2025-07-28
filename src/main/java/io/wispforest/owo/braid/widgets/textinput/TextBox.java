package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.KeyboardInput;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class TextBox extends StatefulWidget {

    public final TextEditingController controller;
    private boolean softWrap = true;
    private boolean autoFocus = false;
    private int maxLines = -1;
    private int maxCharacters = -1;
    private Style baseStyle = Style.EMPTY;
    private Text suggestion = Text.empty();

    public TextBox(
        TextEditingController controller,
        WidgetSetupCallback<TextBox> setupCallback
    ) {
        this.controller = controller;
        setupCallback.setup(this);
    }

    public TextBox softWrap(boolean softWrap) {
        this.assertMutable();
        this.softWrap = softWrap;
        return this;
    }

    public boolean softWrap() {
        return this.softWrap;
    }

    public TextBox autoFocus(boolean autoFocus) {
        this.assertMutable();
        this.autoFocus = autoFocus;
        return this;
    }

    public boolean autoFocus() {
        return this.autoFocus;
    }

    public TextBox maxLines(int maxLines) {
        this.assertMutable();
        this.maxLines = maxLines;
        return this;
    }

    public int maxLines() {
        return this.maxLines;
    }

    public TextBox maxCharacters(int maxCharacters) {
        this.assertMutable();
        this.maxCharacters = maxCharacters;
        return this;
    }

    public int maxCharacters() {
        return this.maxCharacters;
    }

    public TextBox baseStyle(Style baseStyle) {
        this.assertMutable();
        this.baseStyle = baseStyle;
        return this;
    }

    public Style baseStyle() {
        return this.baseStyle;
    }

    public TextBox suggestion(Text suggestion) {
        this.assertMutable();
        this.suggestion = suggestion;
        return this;
    }

    public Text suggestion() {
        return this.suggestion;
    }

    public TextBox placeholder(Text placeholder) {
        return this.suggestion(this.controller.text.isEmpty() ? placeholder : Text.empty());
    }

    public TextBox singleLine() {
        return this
            .softWrap(false)
            .maxLines(1);
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
                                    widget -> widget
                                        .softWrap(this.widget().softWrap)
                                        .autoFocus(this.widget().autoFocus)
                                        .maxLines(this.widget().maxLines)
                                        .maxCharacters(this.widget().maxCharacters)
                                        .baseStyle(this.widget().baseStyle)
                                        .suggestion(this.widget().suggestion)
                                )
                            )
                        )
                    )
                )
            );
        }
    }
}
