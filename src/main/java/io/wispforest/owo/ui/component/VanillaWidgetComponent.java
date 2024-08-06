package io.wispforest.owo.ui.component;

import io.wispforest.owo.mixin.ui.access.AbstractWidgetAccessor;
import io.wispforest.owo.mixin.ui.access.EditBoxAccessor;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class VanillaWidgetComponent extends BaseComponent {

    private final AbstractWidget widget;

    protected VanillaWidgetComponent(AbstractWidget widget) {
        this.widget = widget;

        this.horizontalSizing.set(Sizing.fixed(this.widget.getWidth()));
        this.verticalSizing.set(Sizing.fixed(this.widget.getHeight()));

        if (widget instanceof EditBox) {
            this.margins(Insets.none());
        }
    }

    public boolean hovered() {
        return this.hovered;
    }

    @Override
    public void mount(ParentComponent parent, int x, int y) {
        super.mount(parent, x, y);
        this.applyToWidget();
    }

    @Override
    protected void updateHoveredState(int mouseX, int mouseY, boolean nowHovered) {
        this.hovered = nowHovered;

        if (nowHovered) {
            if (this.root() == null || this.root().childAt(mouseX, mouseY) != this.widget) {
                this.hovered = false;
                return;
            }

            this.mouseEnterEvents.sink().onMouseEnter();
        } else {
            this.mouseLeaveEvents.sink().onMouseLeave();
        }
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        if (this.widget instanceof Button || this.widget instanceof Checkbox || this.widget instanceof SliderComponent) {
            return 20;
        } else if (this.widget instanceof EditBox textField) {
            if (((EditBoxAccessor) textField).owo$bordered()) {
                return 20;
            } else {
                return 9;
            }
        } else if (this.widget instanceof TextAreaComponent textArea && textArea.maxLines() > 0) {
            return MathHelper.clamp(textArea.getInnerHeight() / 9 + 1, 2, textArea.maxLines()) * 9 + (textArea.displayCharCount() ? 9 + 12 : 9);
        } else {
            throw new UnsupportedOperationException(this.widget.getClass().getSimpleName() + " does not support Sizing.content() on the vertical axis");
        }
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        if (this.widget instanceof Button button) {
            return Minecraft.getInstance().font.width(button.getMessage()) + 8;
        } else if (this.widget instanceof Checkbox checkbox) {
            return Minecraft.getInstance().font.width(checkbox.getMessage()) + 24;
        } else {
            throw new UnsupportedOperationException(this.widget.getClass().getSimpleName() + " does not support Sizing.content() on the horizontal axis");
        }
    }

    @Override
    public BaseComponent margins(Insets margins) {
        if (widget instanceof EditBox) {
            return super.margins(margins.add(1, 1, 1, 1));
        } else {
            return super.margins(margins);
        }
    }

    @Override
    public void inflate(Size space) {
        super.inflate(space);
        this.applyToWidget();
    }

    @Override
    public void updateX(int x) {
        super.updateX(x);
        this.applyToWidget();
    }

    @Override
    public void updateY(int y) {
        super.updateY(y);
        this.applyToWidget();
    }

    private void applyToWidget() {
        var accessor = (AbstractWidgetAccessor) this.widget;

        accessor.owo$setX(this.x + this.widget.xOffset());
        accessor.owo$setY(this.y + this.widget.yOffset());

        accessor.owo$setWidth(this.width + this.widget.widthOffset());
        accessor.owo$setHeight(this.height + this.widget.heightOffset());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends Component> C configure(Consumer<C> closure) {
        try {
            this.runAndDeferEvents(() -> closure.accept((C) this.widget));
        } catch (ClassCastException theUserDidBadItWasNotMyFault) {
            throw new IllegalArgumentException(
                    "Invalid target class passed when configuring component of type " + this.getClass().getSimpleName(),
                    theUserDidBadItWasNotMyFault
            );
        }

        return (C) this.widget;
    }

    @Override
    public void notifyParentIfMounted() {
        super.notifyParentIfMounted();
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        this.widget.render(context, mouseX, mouseY, 0);
    }

    @Override
    public boolean shouldDrawTooltip(double mouseX, double mouseY) {
        return this.widget.visible && this.widget.active && super.shouldDrawTooltip(mouseX, mouseY);
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        return this.widget.mouseClicked(this.x + mouseX, this.y + mouseY, button)
                | super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseUp(double mouseX, double mouseY, int button) {
        return this.widget.mouseReleased(this.x + mouseX, this.y + mouseY, button)
                | super.onMouseUp(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return this.widget.mouseScrolled(this.x + mouseX, this.y + mouseY, 0, amount)
                | super.onMouseScroll(mouseX, mouseY, amount);
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        return this.widget.mouseDragged(this.x + mouseX, this.y + mouseY, button, deltaX, deltaY)
                | super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
    }

    @Override
    public boolean onCharTyped(char chr, int modifiers) {
        return this.widget.charTyped(chr, modifiers)
                | super.onCharTyped(chr, modifiers);
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        return this.widget.keyPressed(keyCode, scanCode, modifiers)
                | super.onKeyPress(keyCode, scanCode, modifiers);
    }
}
