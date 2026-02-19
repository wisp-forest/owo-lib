package io.wispforest.owo.compat.rei;

import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.Sizing;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class ReiWidgetComponent extends BaseUIComponent {

    private final WidgetWithBounds widget;

    protected ReiWidgetComponent(WidgetWithBounds widget) {
        this.widget = widget;

        var bounds = widget.getBounds();
        this.horizontalSizing.set(Sizing.fixed(bounds.getWidth()));
        this.verticalSizing.set(Sizing.fixed(bounds.getHeight()));

        this.mouseEnter().subscribe(() -> {
            this.focusHandler().focus(this, FocusSource.KEYBOARD_CYCLE);
        });

        this.mouseLeave().subscribe(() -> {
            this.focusHandler().focus(null, null);
        });
    }

    @Override
    public void mount(ParentUIComponent parent, int x, int y) {
        super.mount(parent, x, y);
        this.applyToWidget();
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        this.widget.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawFocusHighlight(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {}

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return this.widget.getBounds().getWidth();
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return this.widget.getBounds().getHeight();
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
        var bounds = this.widget.getBounds();

        bounds.x = this.x;
        bounds.y = this.y;

        bounds.width = this.width;
        bounds.height = this.height;
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

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }
}
