package io.wispforest.owo.braid.widgets.textinput;

public record MaxLengthFormatter(int maxChars) implements TextInput.Formatter {

    @Override
    public TextEditingValue format(TextEditingValue previousState, TextEditingValue newState) {
        if (newState.text().length() <= this.maxChars) {
            return newState;
        } else if (previousState.text().length() >= this.maxChars) {
            return previousState;
        }

        return new TextEditingValue(
            newState.text().substring(0, this.maxChars),
            newState.selection().upper() > this.maxChars
                ? TextSelection.collapsed(this.maxChars)
                : newState.selection()
        );
    }
}
