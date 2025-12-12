package io.wispforest.owo.ui.component;

import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ui.access.MultiLineEditBoxAccessor;
import io.wispforest.owo.mixin.ui.access.MultilineTextFieldAccessor;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Consumer;

public class TextAreaComponent extends MultiLineEditBox {

    protected final Observable<String> textValue = Observable.of("");
    protected final EventStream<OnChanged> changedEvents = OnChanged.newStream();
    protected final MultilineTextField editBox;

    protected final Observable<Boolean> displayCharCount = Observable.of(false);
    protected final Observable<Integer> maxLines = Observable.of(-1);

    protected TextAreaComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        super(Minecraft.getInstance().font, 0, 0, 0, 0, Component.empty(), Component.empty(), Color.WHITE.argb(), false, Color.WHITE.argb(), true, true);
        this.editBox = ((MultiLineEditBoxAccessor) this).owo$getTextField();
        this.sizing(horizontalSizing, verticalSizing);

        this.textValue.observe(this.changedEvents.sink()::onChanged);
        Observable.observeAll(this.widgetWrapper()::notifyParentIfMounted, this.displayCharCount, this.maxLines);

        super.setValueListener(s -> {
            this.textValue.set(s);

            if (this.maxLines.get() < 0) return;
            this.widgetWrapper().notifyParentIfMounted();
        });
    }

    @Override
    @Deprecated(forRemoval = true)
    public void setValueListener(Consumer<String> changeListener) {
        Owo.debugWarn(Owo.LOGGER, "setChangeListener stub on TextAreaComponent invoked");
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);
        this.cursorStyle(this.scrollbarVisible() && mouseX >= this.getX() + this.width - 9 ? CursorStyle.NONE : CursorStyle.TEXT);
    }

    @Override
    protected void renderDecorations(GuiGraphics context) {
        this.height -= 1;

        var matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(-9, 1);

        int previousMaxLength = this.editBox.characterLimit();
        this.editBox.setCharacterLimit(Integer.MAX_VALUE);

        super.renderDecorations(context);

        this.editBox.setCharacterLimit(previousMaxLength);

        matrices.popMatrix();
        this.height += 1;

        if (this.displayCharCount.get()) {
            var text = this.editBox.hasCharacterLimit()
                    ? Component.translatable("gui.multiLineEditBox.character_limit", this.editBox.value().length(), this.editBox.characterLimit())
                    : Component.literal(String.valueOf(this.editBox.value().length()));

            var textRenderer = Minecraft.getInstance().font;
            context.drawString(textRenderer, text, this.getX() + this.width - textRenderer.width(text), this.getY() + this.height + 3, 0xa0a0a0);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        this.width -= 9;
        var result = super.mouseClicked(click, doubled);
        this.width += 9;

        return result;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        boolean result = super.keyPressed(input);

        if (input.isCycleFocus()) {
            this.editBox.insertText("    ");
            return true;
        } else {
            return result;
        }
    }

    @Override
    public void inflate(Size space) {
        super.inflate(space);

        int cursor = this.editBox.cursor();
        int selection = ((MultilineTextFieldAccessor) this.editBox).owo$getSelectCursor();

        ((MultilineTextFieldAccessor) this.editBox).owo$setWidth(this.width() - this.totalInnerPadding() - 9);
        this.editBox.setValue(this.getValue(), false);

        super.inflate(space);
        this.editBox.setValue(this.getValue(), false);

        this.editBox.seekCursor(Whence.ABSOLUTE, cursor);
        ((MultilineTextFieldAccessor) this.editBox).owo$setSelectCursor(selection);
    }

    public EventSource<OnChanged> onChanged() {
        return changedEvents.source();
    }

    public TextAreaComponent maxLines(int maxLines) {
        this.maxLines.set(maxLines);
        return this;
    }

    public int maxLines() {
        return this.maxLines.get();
    }

    public TextAreaComponent displayCharCount(boolean displayCharCount) {
        this.displayCharCount.set(displayCharCount);
        return this;
    }

    public boolean displayCharCount() {
        return this.displayCharCount.get();
    }

    public TextAreaComponent text(String text) {
        this.setValue(text);
        return this;
    }

    @Override
    public int heightOffset() {
        return this.displayCharCount.get() ? -12 : 0;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(children, "display-char-count", UIParsing::parseBool, this::displayCharCount);
        UIParsing.apply(children, "max-length", UIParsing::parseUnsignedInt, this::setCharacterLimit);
        UIParsing.apply(children, "max-lines", UIParsing::parseUnsignedInt, this::maxLines);
        UIParsing.apply(children, "text", $ -> $.getTextContent().strip(), this::text);
    }

    public interface OnChanged {
        void onChanged(String value);

        static EventStream<OnChanged> newStream() {
            return new EventStream<>(subscribers -> value -> {
                for (var subscriber : subscribers) {
                    subscriber.onChanged(value);
                }
            });
        }
    }
}
