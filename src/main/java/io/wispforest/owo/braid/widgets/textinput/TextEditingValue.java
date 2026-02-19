package io.wispforest.owo.braid.widgets.textinput;

public record TextEditingValue(String text, TextSelection selection) {
    public TextEditingValue withText(String text) {
        return new TextEditingValue(text, this.selection);
    }

    public TextEditingValue withSelection(TextSelection selection) {
        return new TextEditingValue(this.text, selection);
    }
}
