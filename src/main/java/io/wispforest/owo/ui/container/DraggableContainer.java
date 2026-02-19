package io.wispforest.owo.ui.container;

import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.Map;

public class DraggableContainer<C extends UIComponent> extends WrappingParentUIComponent<C> {

    protected int foreheadSize = 10;

    protected int baseX = 0, baseY = 0;
    protected double xOffset = 0, yOffset = 0;

    protected DraggableContainer(Sizing horizontalSizing, Sizing verticalSizing, C child) {
        super(horizontalSizing, verticalSizing, child);
        this.padding(Insets.none());
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(graphics, mouseX, mouseY, partialTicks, delta);
        this.drawChildren(graphics, mouseX, mouseY, partialTicks, delta, this.childView);
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.MOUSE_CLICK;
    }

    @Override
    public boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
        this.xOffset += deltaX;
        this.yOffset += deltaY;

        super.updateX((int) (this.baseX + Math.round(this.xOffset)));
        super.updateY((int) (this.baseY + Math.round(this.yOffset)));
        return super.onMouseDrag(click, deltaX, deltaY);
    }

    @Override
    public @Nullable UIComponent childAt(int x, int y) {
        if (this.isInBoundingBox(x, y) && y - this.y < this.foreheadSize) {
            return this;
        }

        return super.childAt(x, y);
    }

    @Override
    public void updateX(int x) {
        this.baseX = x;
        super.updateX((int) (x + Math.round(this.xOffset)));
    }

    @Override
    public void updateY(int y) {
        this.baseY = y;
        super.updateY((int) (y + Math.round(this.yOffset)));
    }

    @Override
    public int baseX() {
        return this.baseX;
    }

    @Override
    public int baseY() {
        return this.baseY;
    }

    @Override
    public ParentUIComponent padding(Insets padding) {
        return super.padding(Insets.of(padding.top() + this.foreheadSize, padding.bottom(), padding.left(), padding.right()));
    }

    public DraggableContainer<C> foreheadSize(int foreheadSize) {
        int prevForeheadSize = this.foreheadSize;
        this.foreheadSize = foreheadSize;

        var padding = this.padding.get();
        this.padding(Insets.of(padding.top() - prevForeheadSize, padding.bottom(), padding.left(), padding.right()));
        return this;
    }

    public int foreheadSize() {
        return this.foreheadSize;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "forehead-size", UIParsing::parseUnsignedInt, this::foreheadSize);
    }
}
