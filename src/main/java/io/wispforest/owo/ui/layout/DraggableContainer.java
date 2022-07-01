package io.wispforest.owo.ui.layout;

import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.definitions.Insets;
import io.wispforest.owo.ui.definitions.ParentComponent;
import io.wispforest.owo.ui.definitions.Sizing;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

// TODO customize forehead size
public class DraggableContainer<T extends Component> extends WrappingParentComponent<T> {

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
        if (this.isInBoundingBox(x, y) && y - this.y < 10) {
            return this;
        }

        return super.childAt(x, y);
    }

    @Override
    public ParentComponent padding(Insets padding) {
        return super.padding(Insets.of(padding.top() + 10, padding.bottom(), padding.left(), padding.right()));
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
}
