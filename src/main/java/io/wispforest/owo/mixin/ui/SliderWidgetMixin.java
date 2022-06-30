package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.definitions.CursorStyle;
import io.wispforest.owo.ui.parsing.OwoUIParsing;
import io.wispforest.owo.ui.parsing.OwoUISpec;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.w3c.dom.Element;

import java.util.Map;

@SuppressWarnings("ConstantConditions")
@Mixin(SliderWidget.class)
public abstract class SliderWidgetMixin extends ClickableWidget {
    public SliderWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Override
    public CursorStyle cursorStyle() {
        return CursorStyle.MOVE;
    }

    @Override
    public void parseProperties(OwoUISpec spec, Element element, Map<String, Element> children) {
        super.parseProperties(spec, element, children);
        OwoUIParsing.apply(children, "text", OwoUIParsing::parseText, ((SliderWidget) (Object) this)::setMessage);
    }
}