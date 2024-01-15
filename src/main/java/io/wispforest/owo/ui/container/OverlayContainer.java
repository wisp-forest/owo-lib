package io.wispforest.owo.ui.container;

import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.util.EventSource;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class OverlayContainer<C extends Component> extends WrappingParentComponent<C> {

    protected boolean closeOnClick = true;
    protected @Nullable EventSource<?>.Subscription exitSubscription = null;

    protected OverlayContainer(C child) {
        super(Sizing.fill(100), Sizing.fill(100), child);

        this.positioning(Positioning.absolute(0, 0));
        this.surface(Surface.VANILLA_TRANSLUCENT);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        this.drawChildren(context, mouseX, mouseY, partialTicks, delta, this.childView);
    }

    @Override
    public void drawFocusHighlight(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {}

    @Override
    public void mount(ParentComponent parent, int x, int y) {
        super.mount(parent, x, y);
        this.exitSubscription = this.root().keyPress().subscribe((keyCode, scanCode, modifiers) -> {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
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
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        boolean handled = super.onMouseDown(mouseX, mouseY, button) || this.child.isInBoundingBox(mouseX, mouseY);

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
        return this.padding.get().left() + (this.width - this.child.fullSize().width()) / 2;
    }

    @Override
    protected int childMountY() {
        return this.padding.get().top() + (this.height() - this.child.fullSize().height()) / 2;
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
