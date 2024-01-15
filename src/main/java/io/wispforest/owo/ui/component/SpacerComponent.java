package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.w3c.dom.Element;

public class SpacerComponent extends BaseComponent {

    protected SpacerComponent(int percent) {
        this.sizing(Sizing.expand(percent));
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {}

    public static SpacerComponent parse(Element element) {
        if (!element.hasAttribute("percent")) return Components.spacer();
        return Components.spacer(UIParsing.parseUnsignedInt(element.getAttributeNode("percent")));
    }
}
