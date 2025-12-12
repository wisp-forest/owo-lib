package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.ListenableValue;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class TextEditingController extends ListenableValue<TextEditingValue> {

    public TextEditingController(String text, TextSelection selection) {
        super(new TextEditingValue(text, selection));
    }

    public TextEditingController(String text) {
        this(text, TextSelection.collapsed(text.length()));
    }

    public TextEditingController() {
        this("");
    }

    public Component createTextForRendering(Style baseStyle) {
        return Component.literal(this.value().text()).withStyle(style -> baseStyle.applyTo(baseStyle));
    }
}
