package io.wispforest.owo.ui.component;

import io.wispforest.owo.mixin.ui.access.ClickableWidgetAccessor;
import io.wispforest.owo.mixin.ui.access.TextFieldWidgetAccessor;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.*;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

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
    public int determineVerticalContentSize(Sizing sizing) {
        if (this.widget instanceof ButtonWidget || this.widget instanceof CheckboxWidget || this.widget instanceof SliderComponent) {
            return 20;
        } else if (this.widget instanceof TextFieldWidget textField) {
            if (((TextFieldWidgetAccessor) textField).owo$drawsBackground()) {
                return 20;
            } else {
                return 9;
            }
        } else if (this.widget instanceof TextAreaComponent textArea && textArea.maxLines() > 0) {
            return MathHelper.clamp(textArea.getContentsHeight() / 9 + 1, 2, textArea.maxLines()) * 9 + (textArea.displayCharCount() ? 9 + 12 : 9);
        } else {
            throw new UnsupportedOperationException(this.widget.getClass().getSimpleName() + " does not support Sizing.content() on the vertical axis");
        }
    }

    @Override
    public int determineHorizontalContentSize(Sizing sizing) {
        if (this.widget instanceof ButtonWidget button) {
            return MinecraftClient.getInstance().textRenderer.getWidth(button.getMessage()) + 8;
        } else if (this.widget instanceof CheckboxWidget checkbox) {
            return MinecraftClient.getInstance().textRenderer.getWidth(checkbox.getMessage()) + 24;
        } else {
            throw new UnsupportedOperationException(this.widget.getClass().getSimpleName() + " does not support Sizing.content() on the horizontal axis");
        }
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
        var accessor = (ClickableWidgetAccessor) this.widget;

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
