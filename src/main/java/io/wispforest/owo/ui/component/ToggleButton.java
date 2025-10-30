package io.wispforest.owo.ui.component;

import io.wispforest.owo.mixin.ui.access.ButtonWidgetAccessor;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ToggleButton extends IncrementButton {
    protected static final Text ENABLED_MESSAGE = Text.translatable("text.owo.config.boolean_toggle.enabled");
    protected static final Text DISABLED_MESSAGE = Text.translatable("text.owo.config.boolean_toggle.disabled");

    protected Text enabledMessage = ENABLED_MESSAGE;
    protected Text disabledMessage = DISABLED_MESSAGE;

    public ToggleButton(Text baseMessage, Consumer<ButtonComponent> onPress) {
        super(baseMessage, onPress, 1,0, 2);
    }

    public Text enabledMessage() {
        return this.enabledMessage;
    }

    public Text disabledMessage() {
        return this.disabledMessage;
    }

    public ToggleButton enabledMessage(Text text) {
        this.enabledMessage = text;

        return this;
    }

    public ToggleButton disabledMessage(Text text) {
        this.disabledMessage = text;

        return this;
    }

    public ToggleButton renderer(ValueRenderer<ToggleButton> renderer) {
        this.renderer = (ValueRenderer<IncrementButton>) (Object) renderer;

        return this;
    }

    public ToggleButton onPress(BiConsumer<ToggleButton, Boolean> onPress) {
        this.onPress(btnComp -> onPress.accept(this, this.enabled()));

        return this;
    }

    public boolean enabled() {
        return getValue() == 1;
    }

    public ToggleButton enabled(boolean value) {
        setValue(value ? 1 : 0);

        return this;
    }

    @Override
    protected Text getValueText() {
        return enabled() ? ENABLED_MESSAGE : DISABLED_MESSAGE;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(children, "enabled-text", UIParsing::parseText, this::enabledMessage);
        UIParsing.apply(children, "disabled-text", UIParsing::parseText, this::disabledMessage);
    }
}
