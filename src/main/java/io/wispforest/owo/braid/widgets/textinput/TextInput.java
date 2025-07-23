package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.core.TextLayout;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.instance.KeyboardListener;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.instance.MouseListener;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.regex.Pattern;

public class TextInput extends LeafInstanceWidget {

    public final TextEditingController controller;
    public final boolean showCursor;
    public final boolean softWrap;
    public final boolean autoFocus;
    public final boolean allowMultipleLines;
    public final Style baseStyle;

    public TextInput(TextEditingController controller, boolean showCursor, boolean softWrap, boolean autoFocus, boolean allowMultipleLines, Style baseStyle) {
        this.controller = controller;
        this.showCursor = showCursor;
        this.softWrap = softWrap;
        this.autoFocus = autoFocus;
        this.allowMultipleLines = allowMultipleLines;
        this.baseStyle = baseStyle;
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<TextInput> implements MouseListener, KeyboardListener {

        protected static final Pattern LINE_BREAKS = Pattern.compile("[\r\n]");

        protected String text;
        protected TextSelection selection;

        protected String layoutText;
        protected TextSelection layoutSelection;

        protected CursorLocation cursorLocation;

        protected TextLayout.EditMetrics metrics = null;
        protected List<OrderedText> renderLines = List.of();

        public Instance(TextInput widget) {
            super(widget);
            this.layoutText = this.text = widget.controller.text();
            this.layoutSelection = this.selection = widget.controller.selection;

            if (this.widget().autoFocus) {
                this.requestFocus();
            }
        }

        public Vector2d cursorPosition() {
            return this.coordinatesAtCharIdx(this.selection.end());
        }

        public TextLayout.LineMetrics currentLine() {
            return this.metrics.lineMetrics().get(this.cursorLocation.line);
        }

        @Override
        public void setWidget(TextInput widget) {
            if (!(this.layoutText.equals(widget.controller.text())
                  && this.layoutSelection.equals(widget.controller.selection())
                  && this.widget.softWrap == widget.softWrap
                  && this.widget.allowMultipleLines == widget.allowMultipleLines
                  && this.widget.baseStyle.equals(widget.baseStyle))) {

                this.layoutText = this.text = widget.controller.text();
                this.layoutSelection = this.selection = widget.controller.selection();

                this.markNeedsLayout();
            }

            super.setWidget(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var maxWidth = (int) (constraints.hasBoundedWidth() ? constraints.maxWidth() : constraints.minWidth());
            var wrapWidth = this.widget.softWrap ? maxWidth - 2 : Integer.MAX_VALUE;

            this.metrics = TextLayout.measure(
                this.host().client().textRenderer,
                this.text,
                this.widget.baseStyle,
                wrapWidth
            );

            this.renderLines = new ArrayList<>(this.host().client().textRenderer.wrapLines(
                this.widget.controller.createTextForRendering(this.widget.baseStyle),
                wrapWidth
            ));

            var size = Size.of(
                this.metrics.width() + 1,
                this.metrics.height()
            ).constrained(constraints);

            this.transform.setSize(size);

            var newLineIdx = this.lineIdxAtCharIdx(this.selection.end());
            this.cursorLocation = new CursorLocation(newLineIdx, this.selection.end() - this.metrics.lineMetrics().get(newLineIdx).beginIdx());
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return TextLayout.measure(
                this.host().client().textRenderer,
                this.text,
                this.widget.baseStyle,
                Integer.MAX_VALUE
            ).width();
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return TextLayout.measure(
                this.host().client().textRenderer,
                this.text,
                this.widget.baseStyle,
                this.widget.softWrap ? (int) width : Integer.MAX_VALUE
            ).height();
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.of(this.host().client().textRenderer.fontHeight - 2);
        }

        private void drawSelection(OwoUIDrawContext ctx, int startRune, int endRune) {
            var startX = this.coordinatesAtCharIdx(startRune).x;
            var endPos = this.coordinatesAtCharIdx(endRune);

            var height = this.host().client().textRenderer.fontHeight;

            ctx.push();
            ctx.translate(startX, endPos.y - height - 1, 0d);

            var width = endPos.x - startX;
            if (startRune == endRune) width = 2;

            ctx.fill(RenderLayer.getGuiTextHighlight(), 0, 0, (int) width, height, Colors.BLUE);
            ctx.pop();
        }

        @Override
        public void draw(OwoUIDrawContext ctx) {
            var textRenderer = this.host().client().textRenderer;

            for (int lineIdx = 0; lineIdx < this.renderLines.size(); lineIdx++) {
                ctx.drawText(
                    this.host().client().textRenderer,
                    this.renderLines.get(lineIdx),
                    0,
                    lineIdx * textRenderer.fontHeight,
                    Color.WHITE.argb(),
                    false
                );
            }

            // ---

            if (!this.selection.collapsed()) {
                var startLine = this.lineIdxAtCharIdx(this.selection.lower());
                var endLine = this.lineIdxAtCharIdx(this.selection.upper());

                if (startLine == endLine) {
                    drawSelection(ctx, this.selection.lower(), this.selection.upper());
                } else {
                    drawSelection(ctx, this.selection.lower(), this.metrics.lineMetrics().get(startLine).endIdx());
                    for (var lineIdx = startLine + 1; lineIdx < endLine; lineIdx++) {
                        var line = this.metrics.lineMetrics().get(lineIdx);
                        drawSelection(ctx, line.beginIdx(), line.endIdx());
                    }
                    drawSelection(ctx, this.metrics.lineMetrics().get(startLine).beginIdx(), this.selection.upper());
                }
            }

            // ---

            if (this.widget.showCursor) {
                var cursorPos = this.coordinatesAtCharIdx(this.selection.end());

                ctx.drawVerticalLine(
                    (int) cursorPos.x,
                    (int) (cursorPos.y - textRenderer.fontHeight - 2),
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

            final var textRenderer = this.host().client().textRenderer;
            var x = textRenderer.getWidth(this.text.substring(line.beginIdx(), charIdx));
            var y = (lineIdx + 1) * textRenderer.fontHeight;

            return new Vector2d(x, y);
        }

        private void insert(String insertion) {
            if (!this.widget.allowMultipleLines) {
                insertion = LINE_BREAKS.matcher(insertion).replaceAll("");
            }

            var chars = new StringBuilder(this.text);
            chars.replace(this.selection.lower(), this.selection.upper(), insertion);

            this.widget.controller.setText(this.text = chars.toString());
            this.widget.controller.setSelection(this.selection = TextSelection.collapsed(this.selection.lower() + insertion.length()));
        }

        private void deleteSelection() {
            this.insert("");
        }

        private void moveCursorVertically(int byLines, boolean selecting) {
            var newLineIdx = MathHelper.clamp(this.cursorLocation.line + byLines, 0, this.metrics.lineMetrics().size() - 1);
            var currentX = this.cursorPosition().x;

            var newLine = this.metrics.lineMetrics().get(newLineIdx);
            var newLocalRune = 0;

            while (newLocalRune < (newLine.endIdx() - newLine.beginIdx())) {
                var glyphX = this.host().client().textRenderer.getWidth(this.text.substring(newLine.beginIdx(), newLine.beginIdx() + newLocalRune));

                if (glyphX >= currentX) {
                    var previousGlyphX = this.host().client().textRenderer.getWidth(this.text.substring(newLine.beginIdx(), newLine.beginIdx() + Math.max(0, newLocalRune - 1)));

                    if (Math.abs(currentX - previousGlyphX) < Math.abs(currentX - glyphX)) {
                        newLocalRune--;
                    }

                    break;
                }

                newLocalRune++;
            }

            this.moveCursor(newLine.beginIdx() + newLocalRune, selecting);
        }

        private int charIdxAt(double x, double y) {
            var textRenderer = this.host().client().textRenderer;

            var clickedLine = this.metrics.lineMetrics().get(MathHelper.clamp((int) (y / textRenderer.fontHeight), 0, this.metrics.lineMetrics().size() - 1));
            var lineText = this.text.substring(clickedLine.beginIdx(), clickedLine.endIdx());

            return clickedLine.beginIdx() + textRenderer.trimToWidth(lineText, (int) x + 1).length();
        }

        private void moveCursor(int toRune, boolean selecting) {
            if (selecting) {
                widget.controller.setSelection(this.selection = new TextSelection(this.selection.start(), toRune));
            } else {
                widget.controller.setSelection(this.selection = TextSelection.collapsed(toRune));
            }
        }

        private int nextWordBoundary(boolean forwards, OptionalInt fromChar) {
            var fromCharIdx = fromChar.orElse(this.selection.end());

            var direction = forwards ? 1 : -1;
            var lookAhead = forwards ? 0 : -1;
            var bound = forwards ? this.text.length() + 1 : -1;

            var startingClass = SkipClass.of(this.safeCharAt(fromCharIdx + lookAhead));
            var idx = fromCharIdx + direction;

            while (idx != bound && startingClass.shouldSkip(this.safeCharAt(idx + lookAhead))) {
                idx += direction;
            }

            return idx;
        }

        private char safeCharAt(int charIdx) {
            return this.text.charAt(MathHelper.clamp(charIdx, 0, this.text.length() - 1));
        }

        @Override
        public boolean onChar(int charCode, KeyModifiers modifiers) {
            this.insert(Character.toString(charCode));
            return true;
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyModifiers modifiers) {
            var cursorPosition = this.selection.end();

            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!this.selection.collapsed()) {
                    this.deleteSelection();
                } else {
                    var chars = new StringBuilder(this.text);
                    var start = Math.max(
                        0,
                        modifiers.ctrl()
                            ? this.nextWordBoundary(false, OptionalInt.empty())
                            : cursorPosition - 1
                    );
                    chars.delete(start, cursorPosition);

                    this.widget.controller.setSelection(this.selection = TextSelection.collapsed(start));
                    this.widget.controller.setText(this.text = chars.toString());
                }

                return true;
            } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
                if (!this.selection.collapsed()) {
                    this.deleteSelection();
                } else {
                    var chars = new StringBuilder(this.text);
                    var start = Math.max(
                        0,
                        modifiers.shift() && !modifiers.ctrl()
                            ? this.currentLine().beginIdx() - 1
                            : cursorPosition
                    );
                    var end = Math.min(
                        this.text.length(),
                        modifiers.ctrl()
                            ? this.nextWordBoundary(true, OptionalInt.empty())
                            : modifiers.shift()
                                ? this.currentLine().endIdx()
                                : cursorPosition + 1
                    );

                    chars.delete(start, end);

                    this.widget.controller.setSelection(this.selection = TextSelection.collapsed(start));
                    this.widget.controller.setText(this.text = chars.toString());
                }

                return true;
            } else if (keyCode == GLFW.GLFW_KEY_V && modifiers.ctrl()) {
                this.insert(MinecraftClient.getInstance().keyboard.getClipboard());

                return true;
            } else if ((keyCode == GLFW.GLFW_KEY_C || keyCode == GLFW.GLFW_KEY_X) && modifiers.ctrl()) {
                MinecraftClient.getInstance().keyboard.setClipboard(this.text.substring(this.selection.lower(), this.selection.upper()));

                if (keyCode == GLFW.GLFW_KEY_X) {
                    this.deleteSelection();
                }

                return true;
            } else if (keyCode == GLFW.GLFW_KEY_A && modifiers.ctrl()) {
                this.widget.controller.setSelection(this.selection = new TextSelection(0, this.text.length()));
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
                var endingSelection = !this.selection.collapsed() && !modifiers.shift();
                this.moveCursor(
                    Math.max(
                        0,
                        endingSelection
                            ? this.selection.lower()
                            : modifiers.ctrl()
                                ? this.nextWordBoundary(false, OptionalInt.empty())
                                : cursorPosition - 1
                    ),
                    modifiers.shift()
                );
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                var endingSelection = !this.selection.collapsed() && !modifiers.shift();
                this.moveCursor(
                    Math.min(
                        this.text.length(),
                        endingSelection
                            ? this.selection.upper()
                            : modifiers.ctrl()
                                ? this.nextWordBoundary(true, OptionalInt.empty())
                                : cursorPosition + 1
                    ),
                    modifiers.shift()
                );
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_HOME) {
                this.moveCursor(this.currentLine().beginIdx(), modifiers.shift());
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_END) {
                this.moveCursor(this.currentLine().endIdx(), modifiers.shift());
                return true;
            }

            if (this.widget.allowMultipleLines) {
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    this.insert("\n");
                    return true;
                } else if (keyCode == GLFW.GLFW_KEY_UP) {
                    this.moveCursorVertically(-1, modifiers.shift());
                    return true;
                } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                    this.moveCursorVertically(1, modifiers.shift());
                    return true;
                }
            }

            return false;
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

                widget.controller.setSelection(new TextSelection(Math.max(0, start), end));
            } else {
                this.lastClickTime = Instant.now();
                this.moveCursor(clickedIdx, Screen.hasShiftDown());
            }

            return true;
        }

        @Override
        public void onMouseDrag(double x, double y, double dx, double dy) {
            this.moveCursor(this.charIdxAt(x, y), true);
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
