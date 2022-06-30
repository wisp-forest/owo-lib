package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.definitions.CursorStyle;
import io.wispforest.owo.ui.inject.ButtonWidgetExtension;
import io.wispforest.owo.ui.parsing.OwoUIParsing;
import io.wispforest.owo.ui.parsing.OwoUISpec;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.w3c.dom.Element;

import java.util.Map;

@SuppressWarnings("ConstantConditions")
@Mixin(ButtonWidget.class)
public abstract class ButtonWidgetMixin extends ClickableWidget implements ButtonWidgetExtension {
    @Mutable
    @Shadow
    @Final
    protected ButtonWidget.PressAction onPress;

    public ButtonWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Override
    public void parseProperties(OwoUISpec spec, Element element, Map<String, Element> children) {
        super.parseProperties(spec, element, children);
        OwoUIParsing.apply(children, "text", OwoUIParsing::parseText, ((ButtonWidget) (Object) this)::setMessage);
    }

    @Override
    public CursorStyle cursorStyle() {
        return CursorStyle.HAND;
    }

    @Override
    public ButtonWidget onPress(ButtonWidget.PressAction pressAction) {
        this.onPress = pressAction;
        return (ButtonWidget) (Object) this;
    }
}
