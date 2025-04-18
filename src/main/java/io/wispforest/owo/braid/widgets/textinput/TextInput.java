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
    public final boolean softWrap;
    public final boolean allowMultipleLines;

    public TextInput(TextEditingController controller, boolean softWrap, boolean allowMultipleLines) {
        this.controller = controller;
        this.softWrap = softWrap;
        this.allowMultipleLines = allowMultipleLines;
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<TextInput> implements MouseListener, KeyboardListener {

        protected TextEditingController controller;
        protected List<Line> wrappedLines = List.of();

        public Instance(TextInput widget) {
            super(widget);
            this.controller = widget.controller;
        }

        @Override
        public void setWidget(TextInput widget) {
            super.setWidget(widget);
            this.controller = widget.controller;
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var size = Size.of(
                constraints.hasBoundedWidth() ? constraints.maxWidth() : constraints.minWidth(),
                constraints.hasBoundedHeight() ? constraints.maxHeight() : constraints.minHeight()
            );

            this.transform.setSize(size);

            this.updateWrappedLines();
        }

        @Override
        public void draw(OwoUIDrawContext ctx) {
            var textRenderer = this.host().client().textRenderer;

            var cursorLine = this.wrappedLines.get(this.controller.cursorPosition.row());
            var lineString = this.controller.text.substring(cursorLine.beginIdx, cursorLine.beginIdx + this.controller.cursorPosition.col());

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
                    Text.literal(this.controller.text.substring(line.beginIdx, line.endIdx)),
                    0,
                    lineIdx * textRenderer.fontHeight,
                    1f,
                    Color.WHITE.argb()
                );
            }

            if (this.controller.focused()) {
                ctx.drawVerticalLine(
                    textRenderer.getWidth(lineString),
                    this.controller.cursorPosition.row() * textRenderer.fontHeight - 2,
                    (this.controller.cursorPosition.row() + 1) * textRenderer.fontHeight,
                    0xaad0d0d0
                );
            }

            ctx.pop();
            ScissorStack.pop();
        }

        protected void updateWrappedLines() {
             var wrapped = new ArrayList<Line>();

            if (this.widget.allowMultipleLines) {
                this.host().client().textRenderer.getTextHandler().wrapLines(
                    this.controller.text,
                    this.widget.softWrap ? (int) this.transform.width() : Integer.MAX_VALUE,
                    Style.EMPTY,
                    false,
                    (style, start, end) -> wrapped.add(new Line(start, end))
                );

                if (this.controller.text.endsWith("\n")) {
                    wrapped.add(new Line(this.controller.text.length(), this.controller.text.length()));
                }

                if (wrapped.isEmpty()) {
                    wrapped.add(new Line(0, 0));
                }
            } else {
                wrapped.add(new Line(0, this.controller.text.length()));
            }

            this.wrappedLines = wrapped;
            this.recomputeCursorPos(this.controller.cursorPosition.idx());
        }

        private void recomputeCursorPos(int cursorIdx) {
            for (int lineIdx = 0; lineIdx < this.wrappedLines.size(); lineIdx++) {
                var line = this.wrappedLines.get(lineIdx);
                if (cursorIdx >= line.beginIdx && cursorIdx <= line.endIdx) {
                    this.controller.cursorPosition = new CursorPosition(
                        cursorIdx,
                        cursorIdx - line.beginIdx,
                        lineIdx
                    );

                    break;
                }
            }
        }

        protected Line currentLine() {
            return this.wrappedLines.get(this.controller.cursorPosition.row());
        }

        protected int nextWordBoundary(boolean forwards) {
            var direction = forwards ? 1 : -1;
            var lookAhead = forwards ? 0 : -1;
            var bound = forwards ? this.controller.text.length() + 1 : -1;

            var startingClass = SkipClass.of(this.safeCharAt(this.controller.cursorPosition.idx()));
            var idx = this.controller.cursorPosition.idx() + direction;

            if (startingClass != SkipClass.LineBreakClass.INSTANCE) {
                startingClass = SkipClass.of(this.safeCharAt(idx + lookAhead));
            }

            while (idx != bound && startingClass.shouldSkip(this.safeCharAt(idx + lookAhead))) idx += direction;

            return idx;
        }

        protected char safeCharAt(int idx) {
            return idx >= 0 && idx < this.controller.text.length() ? this.controller.text.charAt(idx) : ' ';
        }

        protected void moveCursorVertically(int byLines) {
            var cursorPosition = this.controller.cursorPosition;
            var newRow = MathHelper.clamp(cursorPosition.row() + byLines, 0, this.wrappedLines.size() - 1);

            var renderer = this.host().client().textRenderer;
            var currentWidth = renderer.getWidth(this.controller.text.substring(this.currentLine().beginIdx, cursorPosition.idx())) + 2;

            var newLine = this.wrappedLines.get(newRow);
            var newCol = renderer.trimToWidth(newLine.substring(this.controller.text), currentWidth).length();
            var newIdx = newLine.beginIdx + newCol;

            this.controller.cursorPosition = new CursorPosition(
                newIdx,
                newCol,
                newRow
            );
        }

        @Override
        public void onKeyDown(int keyCode, int modifiers) {
            var hasCtrl = modifiers == GLFW.GLFW_MOD_CONTROL;
            var cursorPosition = this.controller.cursorPosition;

            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (cursorPosition.idx() > 0) {
                    this.erase(hasCtrl ? cursorPosition.idx() - this.nextWordBoundary(false) : 1);
                }
            }

            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                if (cursorPosition.idx() < this.controller.text.length()) {
                    this.erase(hasCtrl ? cursorPosition.idx() - this.nextWordBoundary(true) : -1);
                }
            }

            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                if (hasCtrl) {
                    this.recomputeCursorPos(this.nextWordBoundary(false));
                } else if (cursorPosition.idx() > 0) {
                    this.recomputeCursorPos(cursorPosition.idx() - 1);
                }
            }

            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                if (hasCtrl) {
                    this.recomputeCursorPos(this.nextWordBoundary(true));
                } else if (cursorPosition.idx() < this.controller.text.length()) {
                    this.recomputeCursorPos(cursorPosition.idx() + 1);
                }
            }

            if (keyCode == GLFW.GLFW_KEY_HOME) {
                this.recomputeCursorPos(this.currentLine().beginIdx);
            }

            if (keyCode == GLFW.GLFW_KEY_END) {
                this.recomputeCursorPos(this.currentLine().endIdx);
            }

            if (this.widget.allowMultipleLines) {
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    this.insert("\n");
                }

                if (keyCode == GLFW.GLFW_KEY_UP) {
                    if (cursorPosition.row() > 0) {
                        this.moveCursorVertically(-1);
                    }
                }

                if (keyCode == GLFW.GLFW_KEY_DOWN) {
                    if (cursorPosition.row() < this.wrappedLines.size() - 1) {
                        this.moveCursorVertically(1);
                    }
                }
            }

            if (keyCode == GLFW.GLFW_KEY_V && hasCtrl) {
                var clipboard = GLFW.glfwGetClipboardString(this.host().client().getWindow().getHandle());

                if (clipboard != null) {
                    this.insert(clipboard);
                }
            }
        }

        @Override
        public @Nullable CursorStyle cursorStyleAt(double x, double y) {
            return CursorStyle.TEXT;
        }

        @Override
        public boolean onMouseDown(double x, double y) {
            var renderer = this.host().client().textRenderer;

            var line = this.wrappedLines.get(Math.min((int) (y / renderer.fontHeight), this.wrappedLines.size() - 1));
            var lineText = this.controller.text.substring(line.beginIdx, line.endIdx);
            var clickIdx = renderer.trimToWidth(lineText, (int) x + 1).length();

            this.recomputeCursorPos(line.beginIdx + clickIdx);

            return true;
        }

        @Override
        public void onFocusGained() {
            this.controller.focused = true;
            this.controller.notifyListeners();
        }

        @Override
        public void onFocusLost() {
            this.controller.focused = false;
            this.controller.notifyListeners();
        }

        protected void erase(int count) {
            var oldText = this.controller.text;
            this.controller.text = count >= 0
                ? oldText.substring(0, Math.max(0, this.controller.cursorPosition.idx() - count))
                + oldText.substring(this.controller.cursorPosition.idx())
                : oldText.substring(0, this.controller.cursorPosition.idx())
                + oldText.substring(Math.min(this.controller.cursorPosition.idx() - count, oldText.length()));

            this.controller.cursorPosition = new CursorPosition(
                this.controller.cursorPosition.idx() - Math.max(count, 0),
                this.controller.cursorPosition.col(),
                this.controller.cursorPosition.row()
            );

            this.controller.notifyListeners();
            this.updateWrappedLines();
        }

        protected void insert(String insertion) {
            this.controller.text =
                this.controller.text.substring(0, this.controller.cursorPosition.idx())
                    + insertion
                    + this.controller.text.substring(this.controller.cursorPosition.idx());

            this.controller.cursorPosition = new CursorPosition(
                this.controller.cursorPosition.idx() + insertion.length(),
                this.controller.cursorPosition.col(),
                this.controller.cursorPosition.row()
            );

            this.controller.notifyListeners();
            this.updateWrappedLines();
        }

        @Override
        public void onChar(int charCode, int modifiers) {
            this.insert(Character.toString(charCode));
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
    }
}
