package io.wispforest.owo.ui.layout;

import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.definitions.Insets;
import io.wispforest.owo.ui.definitions.ParentComponent;
import io.wispforest.owo.ui.definitions.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.Map;

public class DraggableContainer<T extends Component> extends WrappingParentComponent<T> {

    protected int foreheadSize = 10;

    protected int baseX = 0, baseY = 0;
    protected double xOffset = 0, yOffset = 0;

    public DraggableContainer(Sizing horizontalSizing, Sizing verticalSizing, T child) {
        super(horizontalSizing, verticalSizing, child);
        this.padding(Insets.none());
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        this.drawClipped(matrices, !this.allowOverflow, () -> this.child.draw(matrices, mouseX, mouseY, partialTicks, delta));
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.MOUSE_CLICK;
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        this.xOffset += deltaX;
        this.yOffset += deltaY;

        super.setX((int) (this.baseX + Math.round(this.xOffset)));
        super.setY((int) (this.baseY + Math.round(this.yOffset)));
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
    public void setX(int x) {
        this.baseX = x;
        super.setX((int) (x + Math.round(this.xOffset)));
    }

    @Override
    public void setY(int y) {
        this.baseY = y;
        super.setY((int) (y + Math.round(this.yOffset)));
    }

    @Override
    public ParentComponent padding(Insets padding) {
        return super.padding(Insets.of(padding.top() + this.foreheadSize, padding.bottom(), padding.left(), padding.right()));
    }

    public DraggableContainer<T> foreheadSize(int foreheadSize) {
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
