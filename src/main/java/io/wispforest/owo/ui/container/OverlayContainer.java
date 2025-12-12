package io.wispforest.owo.ui.container;

import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.util.EventSource;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;

public class OverlayContainer<C extends UIComponent> extends WrappingParentUIComponent<C> {

    protected boolean closeOnClick = true;
    protected @Nullable EventSource<?>.Subscription exitSubscription = null;

    protected OverlayContainer(C child) {
        super(Sizing.fill(100), Sizing.fill(100), child);

        this.positioning(Positioning.absolute(0, 0));
        this.surface(Surface.VANILLA_TRANSLUCENT);
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(graphics, mouseX, mouseY, partialTicks, delta);
        this.drawChildren(graphics, mouseX, mouseY, partialTicks, delta, this.childView);
    }

    @Override
    public void drawFocusHighlight(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {}

    @Override
    public void mount(ParentUIComponent parent, int x, int y) {
        super.mount(parent, x, y);
        this.exitSubscription = this.root().keyPress().subscribe((input) -> {
            if (input.isEscape()) {
                this.remove();
                return true;
            }

            return false;
        });
    }

    @Override
    public void dismount(DismountReason reason) {
        super.dismount(reason);

        if (this.exitSubscription != null) {
            this.exitSubscription.cancel();
        }
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        boolean handled = super.onMouseDown(click, doubled) || this.child.isInBoundingBox(click.x(), click.y());

        if (!handled && this.closeOnClick) {
            this.remove();
            return true;
        } else {
            return handled;
        }
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        super.onMouseScroll(mouseX, mouseY, amount);
        return true;
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.KEYBOARD_CYCLE;
    }

    @Override
    protected int childMountX() {
        return this.x + this.padding.get().left() + (this.width - this.child.fullSize().width()) / 2;
    }

    @Override
    protected int childMountY() {
        return this.y + this.padding.get().top() + (this.height() - this.child.fullSize().height()) / 2;
    }

    /**
     * Set whether this overlay should close when a mouse
     * click occurs outside the bounds of its contents
     */
    public OverlayContainer<C> closeOnClick(boolean closeOnClick) {
        this.closeOnClick = closeOnClick;
        return this;
    }

    /**
     * Whether this overlay should close when a mouse
     * click occurs outside the bounds of its contents
     */
    public boolean closeOnClick() {
        return closeOnClick;
    }
}
