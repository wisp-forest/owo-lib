package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SliderComponent extends SliderWidget {

    protected final EventStream<OnChanged> changedEvents = OnChanged.newStream();
    protected final EventStream<OnSlideEnd> slideEndEvents = OnSlideEnd.newStream();

    protected Function<String, Text> messageProvider = value -> Text.empty();
    protected double scrollStep = .05;

    protected SliderComponent(Sizing horizontalSizing) {
        super(0, 0, 0, 0, Text.empty(), 0);

        this.sizing(horizontalSizing, Sizing.fixed(20));
    }

    public SliderComponent value(double value) {
        value = MathHelper.clamp(value, 0, 1);

        if (this.value != value) {
            this.value = value;

            this.updateMessage();
            this.applyValue();
        }

        return this;
    }

    public double value() {
        return this.value;
    }

    /**
     * @deprecated Use {@code onChanged().subscribe(...)} instead
     */
    @Deprecated(forRemoval = true)
    public SliderComponent onChanged(Consumer<Double> listener) {
        this.changedEvents.source().subscribe(listener::accept);
        return this;
    }

    public SliderComponent message(Function<String, Text> messageProvider) {
        this.messageProvider = messageProvider;
        this.updateMessage();
        return this;
    }

    public SliderComponent scrollStep(double scrollStep) {
        this.scrollStep = scrollStep;
        return this;
    }

    public double scrollStep() {
        return this.scrollStep;
    }

    public EventSource<OnChanged> onChanged() {
        return this.changedEvents.source();
    }

    public EventSource<OnSlideEnd> slideEnd() {
        return this.slideEndEvents.source();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.messageProvider.apply(String.valueOf(this.value)));
    }

    @Override
    protected void applyValue() {
        this.changedEvents.sink().onChanged(this.value);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        if (!this.active) return super.onMouseScroll(mouseX, mouseY, amount);

        this.value(this.value + this.scrollStep * amount);

        super.onMouseScroll(mouseX, mouseY, amount);
        return true;
    }

    @Override
    public boolean onMouseUp(double mouseX, double mouseY, int button) {
        this.slideEndEvents.sink().onSlideEnd();
        return super.onMouseUp(mouseX, mouseY, button);
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
    public final void setMessage(Text message) {
        super.setMessage(message);
    }

    public SliderComponent configure(Consumer<SliderComponent> closure) {
        closure.accept(this);
        return this;
    }

    public interface OnChanged {
        void onChanged(double value);

        static EventStream<OnChanged> newStream() {
            return new EventStream<>(listeners -> value -> {
                for (var listener : listeners) {
                    listener.onChanged(value);
                }
            });
        }
    }

    public interface OnSlideEnd {
        void onSlideEnd();

        static EventStream<OnSlideEnd> newStream() {
            return new EventStream<>(listeners -> () -> {
                for (var listener : listeners) {
                    listener.onSlideEnd();
                }
            });
        }
    }
}
