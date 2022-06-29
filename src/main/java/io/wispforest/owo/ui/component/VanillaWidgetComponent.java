package io.wispforest.owo.ui.component;

import io.wispforest.owo.mixin.ui.ClickableWidgetAccessor;
import io.wispforest.owo.ui.BaseComponent;
import io.wispforest.owo.ui.definitions.Insets;
import io.wispforest.owo.ui.definitions.ParentComponent;
import io.wispforest.owo.ui.definitions.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;

public class VanillaWidgetComponent extends BaseComponent {

    private final ClickableWidget widget;

    public VanillaWidgetComponent(ClickableWidget widget) {
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
        if (this.widget instanceof ButtonWidget) {
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
        } else {
            super.applyVerticalContentSizing(sizing);
        }

        this.applyToWidget();
    }

    @Override
    public BaseComponent margins(Insets margins) {
        if (widget instanceof TextFieldWidget) {
            return super.margins(Insets.of(margins.top() + 1, margins.bottom() + 1, margins.left() + 1, margins.right() + 1));
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
        this.widget.x = this.x;
        this.widget.y = this.y;

        this.widget.setWidth(this.width);
        ((ClickableWidgetAccessor) this.widget).owo$setHeight(this.height);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        this.widget.render(matrices, mouseX, mouseY, 0);
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        return this.widget.mouseClicked(this.x + mouseX, this.y + mouseY, button);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return this.widget.mouseScrolled(this.x + mouseX, this.y + mouseY, amount);
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        return this.widget.mouseDragged(this.x + mouseX, this.y + mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean onCharTyped(char chr, int modifiers) {
        return this.widget.charTyped(chr, modifiers);
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        return this.widget.onKeyPress(keyCode, scanCode, modifiers);
    }
}
