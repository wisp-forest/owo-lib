package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SliderComponent extends SliderWidget {

    protected final Observable<Double> listeners;
    protected Function<String, Text> messageProvider;

    protected SliderComponent(Sizing horizontalSizing) {
        super(0, 0, 0, 0, Text.empty(), 0);

        this.messageProvider = value -> Text.empty();
        this.listeners = Observable.of(this.value);

        this.sizing(horizontalSizing, Sizing.fixed(20));
    }

    public SliderComponent value(double value) {
        this.value = value;
        this.updateMessage();
        this.applyValue();
        return this;
    }

    public double value() {
        return this.value;
    }

    public SliderComponent onChanged(Consumer<Double> listener) {
        this.listeners.observe(listener);
        return this;
    }

    public SliderComponent message(Function<String, Text> messageProvider) {
        this.messageProvider = messageProvider;
        this.updateMessage();
        return this;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.messageProvider.apply(String.valueOf(this.value)));
    }

    @Override
    protected void applyValue() {
        this.listeners.set(this.value);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        if (!this.active) return super.onMouseScroll(mouseX, mouseY, amount);

        this.value(MathHelper.clamp(this.value + .05 * amount, 0, 1));

        super.onMouseScroll(mouseX, mouseY, amount);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.active) return false;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return this.active && super.isValidClickButton(button);
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        if (children.containsKey("text")) {
            var node = children.get("text");
            var content = node.getTextContent().strip();

            if (node.getAttribute("translate").equalsIgnoreCase("true")) {
                this.message(value -> Text.translatable(content, value));
            } else {
                var text = Text.literal(content);
                this.message(value -> text);
            }
        }

        UIParsing.apply(children, "value", UIParsing::parseDouble, this::value);
    }

    /**
     * @deprecated Use {@link #message(Function)} instead,
     * as the message set by this method will be overwritten
     * the next time this slider is moved
     */
    @Override
    @Deprecated
    public void setMessage(Text message) {
        super.setMessage(message);
    }
}
