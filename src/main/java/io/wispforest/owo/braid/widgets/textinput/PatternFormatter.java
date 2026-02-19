package io.wispforest.owo.braid.widgets.textinput;

import net.minecraft.util.Mth;

import java.util.regex.Pattern;

public record PatternFormatter(Pattern pattern, String replacement, boolean allow) implements TextInput.Formatter {

    public static PatternFormatter allow(Pattern pattern) {
        return allow(pattern, "");
    }

    public static PatternFormatter allow(Pattern pattern, String replacement) {
        return new PatternFormatter(pattern, replacement, true);
    }

    public static PatternFormatter deny(Pattern pattern) {
        return deny(pattern, "");
    }

    public static PatternFormatter deny(Pattern pattern, String replacement) {
        return new PatternFormatter(pattern, replacement, false);
    }

    @Override
    public TextEditingValue format(TextEditingValue previousState, TextEditingValue newState) {
        var state = new FormatState(newState);

        var lastRegionEnd = 0;
        for (var match : this.pattern.matcher(newState.text()).results().toList()) {
            this.replaceRegion(lastRegionEnd, match.start(), this.allow, newState.text(), state);
            this.replaceRegion(match.start(), match.end(), !this.allow, newState.text(), state);
            lastRegionEnd = match.end();
        }

        this.replaceRegion(lastRegionEnd, newState.text().length(), this.allow, newState.text(), state);

        return new TextEditingValue(
            state.builder.toString(),
            new TextSelection(
                state.selectionStart,
                state.selectionEnd
            )
        );
    }

    private void replaceRegion(int start, int end, boolean regionIsDenied, String input, FormatState state) {
        var replacement = regionIsDenied
            ? (start != end ? this.replacement : "")
            : input.substring(start, end);

        state.builder.append(replacement);

        if (replacement.length() == end - start) {
            return;
        }

        if (state.newValue.selection().start() > start) {
            var startInRegion = Mth.clamp(state.newValue.selection().start(), start, end) - start;
            state.selectionStart += replacement.length() - startInRegion;
        }

        if (state.newValue.selection().end() > start) {
            var endInRegion = Mth.clamp(state.newValue.selection().end(), start, end) - start;
            state.selectionEnd += replacement.length() - endInRegion;
        }
    }

    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\n|\r\n");
    public static final PatternFormatter NO_NEWLINES = PatternFormatter.deny(NEWLINE_PATTERN);

    private static class FormatState {
        public final TextEditingValue newValue;

        public final StringBuilder builder = new StringBuilder();
        public int selectionStart;
        public int selectionEnd;

        public FormatState(TextEditingValue newValue) {
            this.newValue = newValue;
            this.selectionStart = newValue.selection().start();
            this.selectionEnd = newValue.selection().end();
        }
    }
}
