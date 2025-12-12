package io.wispforest.owo.ui.component;

import io.wispforest.owo.mixin.ui.access.AbstractWidgetAccessor;
import io.wispforest.owo.mixin.ui.access.EditBoxAccessor;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class VanillaWidgetComponent extends BaseUIComponent {

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
    public void mount(ParentUIComponent parent, int x, int y) {
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
            return Mth.clamp(textArea.getInnerHeight() / 9 + 1, 2, textArea.maxLines()) * 9 + (textArea.displayCharCount() ? 9 + 12 : 9);
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
    public BaseUIComponent margins(Insets margins) {
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
    public <C extends UIComponent> C configure(Consumer<C> closure) {
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
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        this.widget.render(graphics, mouseX, mouseY, 0);
    }

    @Override
    public boolean shouldDrawTooltip(double mouseX, double mouseY) {
        return this.widget.visible && this.widget.active && super.shouldDrawTooltip(mouseX, mouseY);
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        return this.widget.mouseClicked(new MouseButtonEvent(this.x + click.x(), this.y + click.y(), click.buttonInfo()), doubled)
                | super.onMouseDown(click, doubled);
    }

    @Override
    public boolean onMouseUp(MouseButtonEvent click) {
        return this.widget.mouseReleased(new MouseButtonEvent(this.x + click.x(), this.y + click.y(), click.buttonInfo()))
                | super.onMouseUp(click);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return this.widget.mouseScrolled(this.x + mouseX, this.y + mouseY, 0, amount)
                | super.onMouseScroll(mouseX, mouseY, amount);
    }

    @Override
    public boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
        return this.widget.mouseDragged(new MouseButtonEvent(this.x + click.x(), this.y + click.y(), click.buttonInfo()), deltaX, deltaY)
                | super.onMouseDrag(click, deltaX, deltaY);
    }

    @Override
    public boolean onCharTyped(CharacterEvent input) {
        return this.widget.charTyped(input)
                | super.onCharTyped(input);
    }

    @Override
    public boolean onKeyPress(KeyEvent input) {
        return this.widget.keyPressed(input)
                | super.onKeyPress(input);
    }
}
