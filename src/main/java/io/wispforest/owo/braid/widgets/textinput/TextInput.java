package io.wispforest.owo.braid.widgets.textinput;

import com.google.common.base.Preconditions;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.instance.MouseListener;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class TextInput extends LeafInstanceWidget {

    public final TextEditingController controller;
    public final boolean showCursor;
    public final boolean softWrap;
    public final List<Formatter> formatters;
    public final Style baseStyle;
    public final boolean textShadow;
    public final Component suggestion;

    public TextInput(TextEditingController controller, boolean showCursor, boolean softWrap, List<Formatter> formatters, Style baseStyle, boolean textShadow, @Nullable Component suggestion) {
        this.controller = controller;
        this.showCursor = showCursor;
        this.softWrap = softWrap;
        this.formatters = formatters;
        this.baseStyle = baseStyle;
        this.textShadow = textShadow;
        this.suggestion = suggestion == null ? Component.empty() : suggestion;
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    @FunctionalInterface
    public interface Formatter {
        TextEditingValue format(TextEditingValue previousState, TextEditingValue newState);
    }

    public static class Instance extends LeafWidgetInstance<TextInput> implements MouseListener {

        protected TextEditingValue lastValue;
        protected TextEditingValue value;

        protected CursorLocation cursorLocation;

        protected TextLayout.EditMetrics metrics = null;
        protected List<FormattedCharSequence> renderLines = List.of();

        public Instance(TextInput widget) {
            super(widget);
            this.lastValue = this.value = widget.controller.value();
        }

        public Vector2d cursorPosition() {
            return this.coordinatesAtCharIdx(this.value.selection().end());
        }

        public TextLayout.LineMetrics currentLine() {
            return this.metrics.lineMetrics().get(this.cursorLocation.line);
        }

        @Override
        public void setWidget(TextInput widget) {
            if (!(this.lastValue.equals(widget.controller.value())
                && this.widget.softWrap == widget.softWrap
                && this.widget.baseStyle.equals(widget.baseStyle)
                && this.widget.suggestion.equals(widget.suggestion))) {

                this.lastValue = this.value = widget.controller.value();

                this.markNeedsLayout();
            }

            super.setWidget(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var maxWidth = (int) (constraints.hasBoundedWidth() ? constraints.maxWidth() : constraints.minWidth());
            var wrapWidth = this.widget.softWrap ? maxWidth - 2 : Integer.MAX_VALUE;

            this.metrics = TextLayout.measure(
                this.host().client().font,
                this.value.text() + this.widget.suggestion.getString(),
                this.widget.baseStyle,
                wrapWidth
            );

            this.renderLines = new ArrayList<>(this.host().client().font.split(
                this.widget.controller.createTextForRendering(this.widget.baseStyle).copy().append(this.widget.suggestion),
                wrapWidth
            ));

            var size = Size.of(
                this.metrics.width() + 1,
                this.metrics.height()
            ).constrained(constraints);

            this.transform.setSize(size);

            var newLineIdx = this.lineIdxAtCharIdx(this.value.selection().end());
            this.cursorLocation = new CursorLocation(newLineIdx, this.value.selection().end() - this.metrics.lineMetrics().get(newLineIdx).beginIdx());
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return TextLayout.measure(
                this.host().client().font,
                this.value.text(),
                this.widget.baseStyle,
                Integer.MAX_VALUE
            ).width();
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return TextLayout.measure(
                this.host().client().font,
                this.value.text(),
                this.widget.baseStyle,
                this.widget.softWrap ? (int) width : Integer.MAX_VALUE
            ).height();
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.of(this.host().client().font.lineHeight - 2);
        }

        private void drawSelection(OwoUIGraphics ctx, double startX, double endX, double lineBaseY) {
            var height = this.host().client().font.lineHeight;

            ctx.push();
            ctx.translate(startX, lineBaseY - height);

            var width = endX - startX;
            ctx.fill(RenderPipelines.GUI_TEXT_HIGHLIGHT, 0, 0, (int) width, height, CommonColors.BLUE);

            ctx.pop();
        }

        @Override
        public void draw(BraidGraphics graphics) {
            var font = this.host().client().font;

            for (int lineIdx = 0; lineIdx < this.renderLines.size(); lineIdx++) {
                graphics.drawString(
                    font,
                    this.renderLines.get(lineIdx),
                    0,
                    lineIdx * font.lineHeight,
                    Color.WHITE.argb(),
                    this.widget.textShadow
                );
            }

            // ---

            var selection = this.value.selection();
            if (!selection.collapsed()) {
                var startLine = this.lineIdxAtCharIdx(selection.lower());
                var endLine = this.lineIdxAtCharIdx(selection.upper());

                if (startLine == endLine) {
                    var startPos = this.coordinatesAtCharIdx(selection.lower());
                    var endPos = this.coordinatesAtCharIdx(selection.upper());

                    this.drawSelection(graphics, startPos.x, endPos.x, endPos.y);
                } else {
                    var startPos = this.coordinatesAtCharIdx(selection.lower());
                    this.drawSelection(graphics, startPos.x, this.metrics.lineMetrics().get(startLine).width(), startPos.y);

                    for (var lineIdx = startLine + 1; lineIdx < endLine; lineIdx++) {
                        var line = this.metrics.lineMetrics().get(lineIdx);
                        var width = line.beginIdx() != line.endIdx() ? line.width() : 2;

                        this.drawSelection(graphics, 0, width, (lineIdx + 1) * font.lineHeight);
                    }

                    var endPos = this.coordinatesAtCharIdx(selection.upper());
                    drawSelection(graphics, 0, endPos.x, endPos.y);
                }
            }

            // ---

            if (this.widget.showCursor) {
                var cursorPos = this.coordinatesAtCharIdx(this.value.selection().end());

                graphics.vLine(
                    (int) cursorPos.x,
                    (int) (cursorPos.y - font.lineHeight - 2),
                    (int) (cursorPos.y),
                    0xaad0d0d0
                );
            }
        }

        private int lineIdxAtCharIdx(int charIdx) {
            var matchedLineIdx = -1;
            var lines = this.metrics.lineMetrics();

            for (var lineIdx = 0; lineIdx < lines.size(); lineIdx++) {
                var line = lines.get(lineIdx);
                if (charIdx >= line.beginIdx() && charIdx <= line.endIdx()) {
                    matchedLineIdx = lineIdx;

                    break;
                }
            }

            return matchedLineIdx != -1 ? matchedLineIdx : lines.size() - 1;
        }

        private Vector2d coordinatesAtCharIdx(int charIdx) {
            var lineIdx = this.lineIdxAtCharIdx(charIdx);
            var line = this.metrics.lineMetrics().get(lineIdx);

            var font = this.host().client().font;
            var text = this.value.text();

            var x = font.width(text.substring(line.beginIdx(), Math.min(text.length(), charIdx)));
            var y = (lineIdx + 1) * font.lineHeight;

            return new Vector2d(x, y);
        }

        public void insert(String insertion) {
            insertion = StringUtil.filterText(insertion, true);

            var chars = new StringBuilder(this.value.text());
            var selection = this.value.selection();
            chars.replace(selection.lower(), selection.upper(), insertion);

            var newText = chars.toString();
            this.formatAndSetValue(new TextEditingValue(
                newText,
                TextSelection.collapsed(selection.lower() + insertion.length())
            ));
        }

        private void deleteSelection() {
            if (Owo.DEBUG) {
                Preconditions.checkState(!this.value.selection().collapsed(), "deleteSelection invoked with collapsed selection");
            }

            this.insert("");
        }

        private int lastTextLineIdx() {
            var lastTextLineIdx = 0;
            while (lastTextLineIdx < this.metrics.lineMetrics().size() && this.metrics.lineMetrics().get(lastTextLineIdx).endIdx() < this.value.text().length()) {
                lastTextLineIdx++;
            }

            return lastTextLineIdx;
        }

        private void moveCursorVertically(int byLines, boolean selecting) {
            var newLineIdx = Mth.clamp(this.cursorLocation.line + byLines, 0, this.lastTextLineIdx());
            var currentX = this.cursorPosition().x;

            var newLine = this.metrics.lineMetrics().get(newLineIdx);
            var newLocalRune = 0;

            var text = this.value.text();
            var actualEndIdx = Math.min(newLine.endIdx(), text.length());
            while (newLocalRune < (actualEndIdx - newLine.beginIdx())) {
                var glyphX = this.host().client().font.width(text.substring(newLine.beginIdx(), newLine.beginIdx() + newLocalRune));

                if (glyphX >= currentX) {
                    var previousGlyphX = this.host().client().font.width(text.substring(newLine.beginIdx(), newLine.beginIdx() + Math.max(0, newLocalRune - 1)));

                    if (Math.abs(currentX - previousGlyphX) < Math.abs(currentX - glyphX)) {
                        newLocalRune--;
                    }

                    break;
                }

                newLocalRune++;
            }

            this.setCursorPosition(newLine.beginIdx() + newLocalRune, selecting);
        }

        private int charIdxAt(double x, double y) {
            var font = this.host().client().font;

            var clickedLine = this.metrics.lineMetrics().get(Mth.clamp((int) (y / font.lineHeight), 0, this.lastTextLineIdx()));
            var lineText = this.value.text().substring(clickedLine.beginIdx(), Math.min(clickedLine.endIdx(), this.value.text().length()));

            return clickedLine.beginIdx() + font.plainSubstrByWidth(lineText, (int) x + 1).length();
        }

        private void setCursorPosition(int toRune, boolean selecting) {
            this.formatAndSetValue(this.value.withSelection(
                selecting
                    ? new TextSelection(this.value.selection().start(), toRune)
                    : TextSelection.collapsed(toRune)
            ));
        }

        private int nextWordBoundary(boolean forwards, OptionalInt fromChar) {
            var fromCharIdx = fromChar.orElse(this.value.selection().end());

            var direction = forwards ? 1 : -1;
            var lookAhead = forwards ? 0 : -1;
            var bound = forwards ? this.value.text().length() + 1 : -1;

            var startingClass = SkipClass.of(this.safeCharAt(fromCharIdx + lookAhead));
            var idx = fromCharIdx + direction;

            while (idx != bound && startingClass.shouldSkip(this.safeCharAt(idx + lookAhead))) {
                idx += direction;
            }

            return idx;
        }

        private char safeCharAt(int charIdx) {
            var text = this.value.text();
            return !text.isEmpty() ? text.charAt(Mth.clamp(charIdx, 0, text.length() - 1)) : ' ';
        }

        private void formatAndSetValue(TextEditingValue newValue) {
            var actual = newValue;
            if (!Objects.equals(this.value.text(), newValue.text())) {
                actual = BraidUtils.fold(
                    this.widget.formatters,
                    newValue,
                    (value, formatter) -> formatter.format(this.value, value)
                );
            }

            this.widget.controller.setValue(this.value = actual);
        }

        // ---

        public boolean onChar(int charCode) {
            this.insert(Character.toString(charCode));
            return true;
        }

        public void deleteText(DeleteTextIntent intent) {
            var selection = this.value.selection();
            if (!selection.collapsed()) {
                this.deleteSelection();
                return;
            }

            var text = this.value.text();
            var cursorPosition = selection.end();

            if (intent.forwards()) {
                var chars = new StringBuilder(text);
                var end = Math.min(
                    text.length(),
                    intent.entireWord()
                        ? this.nextWordBoundary(true, OptionalInt.empty())
                        : cursorPosition + 1
                );

                chars.delete(cursorPosition, end);

                this.formatAndSetValue(new TextEditingValue(
                    chars.toString(),
                    TextSelection.collapsed(cursorPosition)
                ));
            } else {
                var chars = new StringBuilder(text);
                var start = Math.max(
                    0,
                    intent.entireWord()
                        ? this.nextWordBoundary(false, OptionalInt.empty())
                        : cursorPosition - 1
                );
                chars.delete(start, cursorPosition);

                this.formatAndSetValue(new TextEditingValue(
                    chars.toString(),
                    TextSelection.collapsed(start)
                ));
            }
        }

        public void moveCursor(MoveCursorIntent intent) {
            var selection = this.value.selection();
            var text = this.value.text();
            var cursorPosition = selection.end();

            var endingSelection = !selection.collapsed() && !intent.selecting();

            switch (intent.direction()) {
                case UP -> this.moveCursorVertically(-1, intent.selecting());
                case DOWN -> this.moveCursorVertically(1, intent.selecting());
                case RIGHT -> this.setCursorPosition(
                    Math.min(
                        text.length(),
                        endingSelection
                            ? selection.upper()
                            : intent.skipWord()
                                ? this.nextWordBoundary(true, OptionalInt.empty())
                                : cursorPosition + 1
                    ),
                    intent.selecting()
                );
                case LEFT -> this.setCursorPosition(
                    Math.max(
                        0,
                        endingSelection
                            ? selection.lower()
                            : intent.skipWord()
                                ? this.nextWordBoundary(false, OptionalInt.empty())
                                : cursorPosition - 1
                    ),
                    intent.selecting()
                );
            }
        }

        public void pasteFromClipboard() {
            this.insert(Minecraft.getInstance().keyboardHandler.getClipboard());
        }

        public void copyToClipboard(CopyTextIntent intent) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.value.text().substring(
                this.value.selection().lower(),
                this.value.selection().upper()
            ));

            if (intent.delete()) {
                this.deleteSelection();
            }
        }

        public void selectAllText() {
            this.formatAndSetValue(this.value.withSelection(new TextSelection(0, this.value.text().length())));
        }

        public void teleportCursor(TeleportCursorIntent intent) {
            if (intent.toStart()) {
                this.setCursorPosition(this.currentLine().beginIdx(), intent.selecting());
            } else {
                this.setCursorPosition(Math.min(this.value.text().length(), this.currentLine().endIdx()), intent.selecting());
            }
        }

        public void deleteLine() {
            var chars = new StringBuilder(this.value.text());
            var line = this.currentLine();

            chars.delete(line.beginIdx(), line.endIdx());
            this.formatAndSetValue(new TextEditingValue(
                chars.toString(),
                TextSelection.collapsed(line.beginIdx())
            ));
        }

        @Override
        public @Nullable CursorStyle cursorStyleAt(double x, double y) {
            return CursorStyle.TEXT;
        }

        private static final Duration MAX_DOUBLE_CLICK_DELAY = Duration.ofMillis(250);
        private Instant lastClickTime = Instant.EPOCH;

        @Override
        public boolean onMouseDown(double x, double y, int button, KeyModifiers modifiers) {
            var clickedIdx = this.charIdxAt(x, y);

            if (Duration.between(this.lastClickTime, Instant.now()).compareTo(MAX_DOUBLE_CLICK_DELAY) < 0) {
                var start = this.nextWordBoundary(false, OptionalInt.of(clickedIdx));
                var end = this.nextWordBoundary(true, OptionalInt.of(clickedIdx));

                this.formatAndSetValue(this.value.withSelection(
                    new TextSelection(Math.max(0, start), end)
                ));
            } else {
                this.lastClickTime = Instant.now();
                this.setCursorPosition(clickedIdx, modifiers.shift());
            }

            return true;
        }

        @Override
        public void onMouseDrag(double x, double y, double dx, double dy) {
            this.setCursorPosition(this.charIdxAt(x, y), true);
        }

        protected interface SkipClass {
            boolean shouldSkip(char c);

            static SkipClass of(char c) {
                if (c == '\n') {
                    return LineBreakClass.INSTANCE;
                }

                if (WordClass.isWordChar(c)) {
                    return WordClass.INSTANCE;
                }

                return new NonWordClass(c);
            }


            enum WordClass implements SkipClass {
                INSTANCE;

                @Override
                public boolean shouldSkip(char c) {
                    return isWordChar(c);
                }

                public static boolean isWordChar(char c) {
                    return c == '_' || Character.isAlphabetic(c) || Character.isDigit(c);
                }
            }

            enum LineBreakClass implements SkipClass {
                INSTANCE;

                @Override
                public boolean shouldSkip(char c) {
                    return false;
                }
            }

            record NonWordClass(char specimen) implements SkipClass {
                @Override
                public boolean shouldSkip(char c) {
                    return c == this.specimen;
                }
            }
        }

        protected record CursorLocation(int line, int charIdx) {}
    }
}
