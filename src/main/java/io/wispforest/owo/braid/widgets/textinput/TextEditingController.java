package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.Listenable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class TextEditingController extends Listenable {

    protected String text;
    protected TextSelection selection;

    public TextEditingController(String text, TextSelection selection) {
        this.text = text;
        this.selection = selection;
    }

    public TextEditingController(String text) {
        this(text, TextSelection.collapsed(text.length()));
    }

    public TextEditingController() {
        this("");
    }

    public void setText(String text) {
        if (this.text.equals(text)) {
            return;
        }

        this.text = text;
        this.notifyListeners();
    }

    public String text() {
        return this.text;
    }

    public void setSelection(TextSelection selection) {
        if (this.selection.equals(selection)) {
            return;
        }

        this.selection = selection;
        this.notifyListeners();
    }

    public TextSelection selection() {
        return this.selection;
    }

    public Text createTextForRendering(Style baseStyle) {
        return Text.literal(this.text).styled(style -> baseStyle.withParent(baseStyle));
    }
}
