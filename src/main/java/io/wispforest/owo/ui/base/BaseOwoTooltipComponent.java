package io.wispforest.owo.ui.base;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Size;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.Experimental
public abstract class BaseOwoTooltipComponent<R extends ParentComponent> implements TooltipComponent {

    protected final R rootComponent;
    protected int virtualWidth = 1000, virtualHeight = 1000;

    protected BaseOwoTooltipComponent(Supplier<R> components) {
        this.rootComponent = components.get();

        this.rootComponent.inflate(Size.of(this.virtualWidth, this.virtualHeight));
        this.rootComponent.mount(null, 0, 0);
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        var client = MinecraftClient.getInstance();

        this.rootComponent.moveTo(x, y);
        this.rootComponent.draw(OwoUIDrawContext.of(context), -1000, -1000, client.getTickDelta(), client.getLastFrameDuration());
    }

    @Override
    public int getHeight() {
        return this.rootComponent.fullSize().height();
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return this.rootComponent.fullSize().width();
    }
}
