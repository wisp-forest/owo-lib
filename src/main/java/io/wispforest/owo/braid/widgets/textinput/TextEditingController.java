package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.ListenableValue;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

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

    public Text createTextForRendering(Style baseStyle) {
        return Text.literal(this.value().text()).styled(style -> baseStyle.withParent(baseStyle));
    }
}
