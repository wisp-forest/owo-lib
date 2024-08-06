package io.wispforest.owo.ui.component;

import io.wispforest.owo.mixin.ui.access.EditBoxAccessor;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Text;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Consumer;

public class TextBoxComponent extends EditBox {

    protected final Observable<Boolean> showsBackground = Observable.of(((EditBoxAccessor) this).owo$bordered());

    protected final Observable<String> textValue = Observable.of("");
    protected final EventStream<OnChanged> changedEvents = OnChanged.newStream();

    protected TextBoxComponent(Sizing horizontalSizing) {
        super(Minecraft.getInstance().font, 0, 0, 0, 0, Text.empty());

        this.textValue.observe(this.changedEvents.sink()::onChanged);
        this.sizing(horizontalSizing, Sizing.content());

        this.showsBackground.observe(a -> this.widgetWrapper().notifyParentIfMounted());
    }

    /**
     * @deprecated Subscribe to {@link #onChanged()} instead
     */
    @Override
    @Deprecated(forRemoval = true)
    public void setResponder(Consumer<String> changedListener) {
        super.setResponder(changedListener);
    }

    @Override
    public void drawFocusHighlight(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        // noop, since TextFieldWidget already does this
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean result = super.keyPressed(keyCode, scanCode, modifiers);

        if (keyCode == GLFW.GLFW_KEY_TAB) {
            this.insertText("    ");
            return true;
        } else {
            return result;
        }
    }

    @Override
    public void setBordered(boolean drawsBackground) {
        super.setBordered(drawsBackground);
        this.showsBackground.set(drawsBackground);
    }

    public EventSource<OnChanged> onChanged() {
        return changedEvents.source();
    }

    public TextBoxComponent text(String text) {
        this.setValue(text);
        this.moveCursorToStart(false);
        return this;
    }

    @Override
    public void parseProperties(UIModel spec, Element element, Map<String, Element> children) {
        super.parseProperties(spec, element, children);
        UIParsing.apply(children, "show-background", UIParsing::parseBool, this::setBordered);
        UIParsing.apply(children, "max-length", UIParsing::parseUnsignedInt, this::setMaxLength);
        UIParsing.apply(children, "text", e -> e.getTextContent().strip(), this::text);
    }

    protected CursorStyle owo$preferredCursorStyle() {
        return CursorStyle.TEXT;
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
