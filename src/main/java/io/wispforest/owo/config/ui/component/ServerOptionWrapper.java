package io.wispforest.owo.config.ui.component;

import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;

public class ServerOptionWrapper<C extends Component> extends WrappingParentComponent<C> {

    public ServerOptionWrapper(C child) {
        super(Sizing.content(), Sizing.content(), child);
        this.tooltip(List.of(
                TooltipComponent.of(Text.literal("This option is currently being handled by the").asOrderedText()),
                TooltipComponent.of(Text.literal("server, disconnect to edit it").asOrderedText())
        ));
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);

        this.drawClipped(matrices, mouseX, mouseY, partialTicks, delta, this.childView);

        Drawer.fill(matrices, this.x, this.y, (int) (this.x + this.width * .75f), this.y + this.height, 0x77000000);
        Drawer.drawGradientRect(matrices, (int) (this.x + this.width * .75f), this.y, this.width / 4, this.height, 0x77000000, 0, 0, 0x77000000);
    }

    @Override
    public void drawTooltip(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.isInBoundingBox(mouseX, mouseY)) return;
        Drawer.drawTooltip(matrices, mouseX, mouseY, this.tooltip);
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean onMouseUp(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return false;
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        return false;
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean onCharTyped(char chr, int modifiers) {
        return false;
    }
}
