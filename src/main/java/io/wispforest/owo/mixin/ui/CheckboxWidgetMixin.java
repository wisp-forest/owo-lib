package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.inject.CheckboxWidgetExtension;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.w3c.dom.Element;

import java.util.Map;

@Mixin(CheckboxWidget.class)
public abstract class CheckboxWidgetMixin extends ClickableWidget implements CheckboxWidgetExtension {

    @Shadow private boolean checked;

    public CheckboxWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Override
    public CheckboxWidget checked(boolean checked) {
        this.checked = checked;
        return (CheckboxWidget) (Object) this;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "checked", UIParsing::parseBool, this::checked);
        UIParsing.apply(children, "text", UIParsing::parseText, this::setMessage);
    }
}
