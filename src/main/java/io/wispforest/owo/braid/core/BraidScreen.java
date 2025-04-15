package io.wispforest.owo.braid.core;

import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.ui.util.DisposableScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class BraidScreen extends Screen implements DisposableScreen {

    protected final Widget rootWidget;
    protected AppState state;

    public BraidScreen(Widget rootWidget) {
        super(Text.empty());
        this.rootWidget = rootWidget;
    }

    @Override
    protected void init() {
        super.init();

        if (this.state == null) {
            this.state = new AppState(
                null,
                this.client,
                this.rootWidget
            );
        } else {
            this.state.rootInstance().markNeedsLayout();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        this.state.updateWidgetsAndInteractions(
            mouseX,
            mouseY,
            this.client.getRenderTickCounter().getTickDelta(false),
            this.client.getRenderTickCounter().getLastFrameDuration()
        );

        this.state.draw(context);
    }

    @Override
    public void dispose() {
        this.state.dispose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.state.dispatchMouseDownEvent(mouseX, mouseY) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.state.dispatchMouseDragEvent(mouseX, mouseY, deltaX, deltaY) || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.state.dispatchMouseUpEvent() || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.state.dispatchMouseScrollEvent(mouseX, mouseY, horizontalAmount, verticalAmount) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.state.dispatchKeyDownEvent(keyCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return this.state.dispatchKeyUpEvent(keyCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.state.dispatchCharEvent(chr, modifiers) || super.charTyped(chr, modifiers);
    }
}
