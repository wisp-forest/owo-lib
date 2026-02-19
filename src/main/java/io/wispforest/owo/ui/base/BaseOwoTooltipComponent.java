package io.wispforest.owo.ui.base;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.Experimental
public abstract class BaseOwoTooltipComponent<R extends ParentUIComponent> implements ClientTooltipComponent {

    protected final R rootComponent;
    protected int virtualWidth = 1000, virtualHeight = 1000;

    protected BaseOwoTooltipComponent(Supplier<R> components) {
        this.rootComponent = components.get();

        this.rootComponent.inflate(Size.of(this.virtualWidth, this.virtualHeight));
        this.rootComponent.mount(null, 0, 0);
    }

    @Override
    public void renderImage(Font textRenderer, int x, int y, int width, int height, GuiGraphics context) {
        var tickCounter = Minecraft.getInstance().getDeltaTracker();

        this.rootComponent.moveTo(x, y);
        this.rootComponent.draw(OwoUIGraphics.of(context), -1000, -1000, tickCounter.getGameTimeDeltaPartialTick(false), tickCounter.getGameTimeDeltaTicks());
    }

    @Override
    public int getHeight(Font textRenderer) {
        return this.rootComponent.fullSize().height();
    }

    @Override
    public int getWidth(Font textRenderer) {
        return this.rootComponent.fullSize().width();
    }
}
