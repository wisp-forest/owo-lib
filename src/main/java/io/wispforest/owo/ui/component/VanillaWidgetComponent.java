package io.wispforest.owo.ui.component;

import io.wispforest.owo.mixin.ui.ClickableWidgetAccessor;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;

public class VanillaWidgetComponent extends BaseComponent {

    private final ClickableWidget widget;

    protected VanillaWidgetComponent(ClickableWidget widget) {
        this.widget = widget;

        this.horizontalSizing.set(Sizing.fixed(this.widget.getWidth()));
        this.verticalSizing.set(Sizing.fixed(this.widget.getHeight()));

        if (widget instanceof TextFieldWidget) {
            this.margins(Insets.none());
        }
    }

    @Override
    public void mount(ParentComponent parent, int x, int y) {
        super.mount(parent, x, y);
        this.applyToWidget();
    }

    @Override
    protected void applyVerticalContentSizing(Sizing sizing) {
        if (this.widget instanceof ButtonWidget || this.widget instanceof CheckboxWidget || this.widget instanceof SliderComponent || this.widget instanceof TextFieldWidget) {
            this.height = 20;
        } else {
            super.applyVerticalContentSizing(sizing);
        }

        this.applyToWidget();
    }

    @Override
    protected void applyHorizontalContentSizing(Sizing sizing) {
        if (this.widget instanceof ButtonWidget button) {
            this.width = MinecraftClient.getInstance().textRenderer.getWidth(button.getMessage()) + 6 + sizing.value * 2;
        } else if (this.widget instanceof CheckboxWidget checkbox) {
            this.width = MinecraftClient.getInstance().textRenderer.getWidth(checkbox.getMessage()) + 24;
        } else {
            super.applyHorizontalContentSizing(sizing);
        }

        this.applyToWidget();
    }

    @Override
    public BaseComponent margins(Insets margins) {
        if (widget instanceof TextFieldWidget) {
            return super.margins(margins.add(1, 1, 1, 1));
        } else {
            return super.margins(margins);
        }
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.applyToWidget();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.applyToWidget();
    }

    private void applyToWidget() {
        var accessor = (ClickableWidgetAccessor) this.widget;

        accessor.owo$setX(this.x);
        accessor.owo$setY(this.y);

        accessor.owo$setWidth(this.width);
        accessor.owo$setHeight(this.height);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        this.widget.render(matrices, mouseX, mouseY, 0);
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
        return this.widget.mouseScrolled(this.x + mouseX, this.y + mouseY, amount)
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
