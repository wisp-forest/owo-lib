package io.wispforest.owo.ui.container;

import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.Map;

public class DraggableContainer<C extends Component> extends WrappingParentComponent<C> {

    protected int foreheadSize = 10;
    protected boolean alwaysOnTop = false;

    protected int baseX = 0, baseY = 0;
    protected double xOffset = 0, yOffset = 0;

    protected DraggableContainer(Sizing horizontalSizing, Sizing verticalSizing, C child) {
        super(horizontalSizing, verticalSizing, child);
        this.padding(Insets.none());
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.alwaysOnTop) context.getMatrices().translate(0, 0, 500);
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        this.drawChildren(context, mouseX, mouseY, partialTicks, delta, this.childView);
        if (this.alwaysOnTop) context.getMatrices().translate(0, 0, -500);
    }

    @Override
    public void drawTooltip(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.alwaysOnTop) context.getMatrices().translate(0, 0, 500);
        super.drawTooltip(context, mouseX, mouseY, partialTicks, delta);
        if (this.alwaysOnTop) context.getMatrices().translate(0, 0, -500);
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.MOUSE_CLICK;
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        this.xOffset += deltaX;
        this.yOffset += deltaY;

        super.updateX((int) (this.baseX + Math.round(this.xOffset)));
        super.updateY((int) (this.baseY + Math.round(this.yOffset)));
        return super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
    }

    @Override
    public @Nullable Component childAt(int x, int y) {
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
    public ParentComponent padding(Insets padding) {
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

    public DraggableContainer<C> alwaysOnTop(boolean alwaysOnTop) {
        this.alwaysOnTop = alwaysOnTop;
        return this;
    }

    public boolean alwaysOnTop() {
        return this.alwaysOnTop;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "forehead-size", UIParsing::parseUnsignedInt, this::foreheadSize);
        UIParsing.apply(children, "always-on-top", UIParsing::parseBool, this::alwaysOnTop);
    }
}
