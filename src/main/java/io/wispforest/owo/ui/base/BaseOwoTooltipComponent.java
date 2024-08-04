package io.wispforest.owo.ui.base;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Size;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

@ApiStatus.Experimental
public abstract class BaseOwoTooltipComponent<R extends ParentComponent> implements ClientTooltipComponent {

    protected final R rootComponent;
    protected int virtualWidth = 1000, virtualHeight = 1000;

    protected BaseOwoTooltipComponent(Supplier<R> components) {
        this.rootComponent = components.get();

        this.rootComponent.inflate(Size.of(this.virtualWidth, this.virtualHeight));
        this.rootComponent.mount(null, 0, 0);
    }

    @Override
    public void renderImage(Font textRenderer, int x, int y, GuiGraphics context) {
        var client = Minecraft.getInstance();

        var tickCounter = Minecraft.getInstance().getTimer();

        this.rootComponent.moveTo(x, y);
        this.rootComponent.draw(OwoUIDrawContext.of(context), -1000, -1000, tickCounter.getGameTimeDeltaPartialTick(false), tickCounter.getGameTimeDeltaTicks());
    }

    @Override
    public int getHeight() {
        return this.rootComponent.fullSize().height();
    }

    @Override
    public int getWidth(Font textRenderer) {
        return this.rootComponent.fullSize().width();
    }
}
