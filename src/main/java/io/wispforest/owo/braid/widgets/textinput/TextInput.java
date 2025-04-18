package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.instance.KeyboardListener;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.instance.MouseListener;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.util.ScissorStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class TextInput extends LeafInstanceWidget {

    public final TextEditingController controller;
    public final boolean showCursor;
    public final boolean softWrap;
    public final boolean allowMultipleLines;

    public TextInput(TextEditingController controller, boolean showCursor, boolean softWrap, boolean allowMultipleLines) {
        this.controller = controller;
        this.showCursor = showCursor;
        this.softWrap = softWrap;
        this.allowMultipleLines = allowMultipleLines;
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<TextInput> implements MouseListener, KeyboardListener {

        protected String text;
        protected int cursorPosition;
        protected CursorLocation cursorLocation;

        protected List<Line> wrappedLines = List.of();

        public Instance(TextInput widget) {
            super(widget);
            this.text = widget.controller.text();
            this.cursorPosition = widget.controller.cursorPosition();
        }

        @Override
        public void setWidget(TextInput widget) {
            if (!(this.text.equals(widget.controller.text())
                && this.cursorPosition == widget.controller.cursorPosition()
                && this.widget.softWrap == widget.softWrap
                && this.widget.allowMultipleLines == widget.allowMultipleLines)) {
                this.markNeedsLayout();
            }

            super.setWidget(widget);
            this.text = widget.controller.text();
            this.cursorPosition = widget.controller.cursorPosition();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var size = Size.of(
                constraints.hasBoundedWidth() ? constraints.maxWidth() : constraints.minWidth(),
                constraints.hasBoundedHeight() ? constraints.maxHeight() : constraints.minHeight()
            );

            this.transform.setSize(size);

            // wrap lines

            var wrapped = new ArrayList<Line>();

            if (this.widget.allowMultipleLines) {
                this.host().client().textRenderer.getTextHandler().wrapLines(
                    this.text,
                    this.widget.softWrap ? (int) this.transform.width() : Integer.MAX_VALUE,
                    Style.EMPTY,
                    false,
                    (style, start, end) -> wrapped.add(new Line(start, end))
                );

                if (this.text.endsWith("\n")) {
                    wrapped.add(new Line(this.text.length(), this.text.length()));
                }

                if (wrapped.isEmpty()) {
                    wrapped.add(new Line(0, 0));
                }
            } else {
                wrapped.add(new Line(0, this.text.length()));
            }

            this.wrappedLines = wrapped;

            // compute cursor location

            for (int lineIdx = 0; lineIdx < this.wrappedLines.size(); lineIdx++) {
                var line = this.wrappedLines.get(lineIdx);
                if (this.cursorPosition >= line.beginIdx && this.cursorPosition <= line.endIdx) {
                    this.cursorLocation = new CursorLocation(
                        this.cursorPosition - line.beginIdx,
                        lineIdx
                    );

                    break;
                }
            }
        }

        @Override
        public void draw(OwoUIDrawContext ctx) {
            var textRenderer = this.host().client().textRenderer;

            var cursorLine = this.wrappedLines.get(this.cursorLocation.row());
            var lineString = this.text.substring(cursorLine.beginIdx, cursorLine.beginIdx + this.cursorLocation.col());

            var cursorX = textRenderer.getWidth(lineString);

            ScissorStack.push(0, 0, (int) this.transform.width(), (int) this.transform.height(), ctx);
            ctx.push();

            ctx.translate(
                -Math.max(0, cursorX - this.transform.width()),
                !this.widget.allowMultipleLines ? Math.round((this.transform.height() - textRenderer.fontHeight) / 2) : 0,
                0
            );

            for (int lineIdx = 0; lineIdx < this.wrappedLines.size(); lineIdx++) {
                var line = this.wrappedLines.get(lineIdx);

                ctx.drawText(
                    Text.literal(this.text.substring(line.beginIdx, line.endIdx)),
                    0,
                    lineIdx * textRenderer.fontHeight,
                    1f,
                    Color.WHITE.argb()
                );
            }

            if (this.widget.showCursor) {
                ctx.drawVerticalLine(
                    textRenderer.getWidth(lineString),
                    this.cursorLocation.row() * textRenderer.fontHeight - 2,
                    (this.cursorLocation.row() + 1) * textRenderer.fontHeight,
                    0xaad0d0d0
                );
            }

            ctx.pop();
            ScissorStack.pop();
        }

        protected Line currentLine() {
            return this.wrappedLines.get(this.cursorLocation.row());
        }

        protected int nextWordBoundary(boolean forwards) {
            var direction = forwards ? 1 : -1;
            var lookAhead = forwards ? 0 : -1;
            var bound = forwards ? this.text.length() + 1 : -1;

            var startingClass = SkipClass.of(this.safeCharAt(this.cursorPosition));
            var idx = this.cursorPosition + direction;

            if (startingClass != SkipClass.LineBreakClass.INSTANCE) {
                startingClass = SkipClass.of(this.safeCharAt(idx + lookAhead));
            }

            while (idx != bound && startingClass.shouldSkip(this.safeCharAt(idx + lookAhead))) idx += direction;

            return idx;
        }

        protected char safeCharAt(int idx) {
            return idx >= 0 && idx < this.text.length() ? this.text.charAt(idx) : ' ';
        }

        protected void moveCursorVertically(int byLines) {
            var newRow = MathHelper.clamp(this.cursorLocation.row() + byLines, 0, this.wrappedLines.size() - 1);

            var renderer = this.host().client().textRenderer;
            var currentWidth = renderer.getWidth(this.text.substring(this.currentLine().beginIdx, this.cursorPosition)) + 2;

            var newLine = this.wrappedLines.get(newRow);
            var newCol = renderer.trimToWidth(newLine.substring(this.text), currentWidth).length();
            var newIdx = newLine.beginIdx + newCol;

            this.widget.controller.setCursorPosition(newIdx);
        }

        @Override
        public boolean onKeyDown(int keyCode, int modifiers) {
            var hasCtrl = modifiers == GLFW.GLFW_MOD_CONTROL;

            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (this.cursorPosition > 0) {
                    this.erase(hasCtrl ? this.cursorPosition - this.nextWordBoundary(false) : 1);
                }

                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                if (this.cursorPosition < this.text.length()) {
                    this.erase(hasCtrl ? this.cursorPosition - this.nextWordBoundary(true) : -1);
                }

                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                if (hasCtrl) {
                    this.widget.controller.setCursorPosition(this.nextWordBoundary(false));
                } else if (this.cursorPosition > 0) {
                    this.widget.controller.setCursorPosition(this.cursorPosition - 1);
                }

                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                if (hasCtrl) {
                    this.widget.controller.setCursorPosition(this.nextWordBoundary(true));
                } else if (this.cursorPosition < this.text.length()) {
                    this.widget.controller.setCursorPosition(this.cursorPosition + 1);
                }

                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_HOME) {
                this.widget.controller.setCursorPosition(this.currentLine().beginIdx);
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_END) {
                this.widget.controller.setCursorPosition(this.currentLine().endIdx);
                return true;
            }

            if (this.widget.allowMultipleLines) {
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    this.insert("\n");
                    return true;
                }

                if (keyCode == GLFW.GLFW_KEY_UP) {
                    if (cursorLocation.row() > 0) {
                        this.moveCursorVertically(-1);
                    }

                    return true;
                }

                if (keyCode == GLFW.GLFW_KEY_DOWN) {
                    if (cursorLocation.row() < this.wrappedLines.size() - 1) {
                        this.moveCursorVertically(1);
                    }

                    return true;
                }
            }

            if (keyCode == GLFW.GLFW_KEY_V && hasCtrl) {
                var clipboard = GLFW.glfwGetClipboardString(this.host().client().getWindow().getHandle());

                if (clipboard != null) {
                    this.insert(clipboard);
                }

                return true;
            }

            return false;
        }

        @Override
        public @Nullable CursorStyle cursorStyleAt(double x, double y) {
            return CursorStyle.TEXT;
        }

        @Override
        public boolean onMouseDown(double x, double y) {
            var renderer = this.host().client().textRenderer;

            var line = this.wrappedLines.get(Math.min((int) (y / renderer.fontHeight), this.wrappedLines.size() - 1));
            var lineText = this.text.substring(line.beginIdx, line.endIdx);
            var clickIdx = renderer.trimToWidth(lineText, (int) x + 1).length();

            this.widget.controller.setCursorPosition(line.beginIdx + clickIdx);

            return true;
        }

        protected void erase(int count) {
            this.widget.controller.setText(
                count >= 0
                    ? this.text.substring(0, Math.max(0, this.cursorPosition - count))
                    + this.text.substring(this.cursorPosition)
                    : this.text.substring(0, this.cursorPosition)
                    + this.text.substring(Math.min(this.cursorPosition - count, this.text.length()))
            );

            this.widget.controller.setCursorPosition(
                this.cursorPosition - Math.max(count, 0)
            );
        }

        protected void insert(String insertion) {
            this.widget.controller.setText(
                this.text.substring(0, this.cursorPosition)
                    + insertion
                    + this.text.substring(this.cursorPosition)
            );

            this.widget.controller.setCursorPosition(
                this.cursorPosition + insertion.length()
            );
        }

        @Override
        public boolean onChar(int charCode, int modifiers) {
            this.insert(Character.toString(charCode));
            return true;
        }

        protected record Line(int beginIdx, int endIdx) {
            public String substring(String fullContent) {
                return fullContent.substring(this.beginIdx, this.endIdx);
            }
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

        protected record CursorLocation(int col, int row) {}
    }
}
