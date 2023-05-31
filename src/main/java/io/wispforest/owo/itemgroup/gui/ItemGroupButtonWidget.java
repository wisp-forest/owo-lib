package io.wispforest.owo.itemgroup.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ItemGroupButtonWidget extends ButtonWidget {

    public boolean isSelected = false;
    private final OwoItemGroup.ButtonDefinition definition;
    private final boolean hoverReactive;

    public ItemGroupButtonWidget(int x, int y, boolean hoverReactive, OwoItemGroup.ButtonDefinition definition, PressAction onPress) {
        super(x, y, 24, 24, definition.tooltip(), onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.definition = definition;
        this.hoverReactive = hoverReactive;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        var client = MinecraftClient.getInstance();

        drawTexture(context, definition.texture(), this.getX(), this.getY(), 0, 0, height, this.width, this.height, 64, 64);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        this.definition.icon().render(context, this.getX() + 4, this.getY() + 4, mouseX, mouseY, delta);

        RenderSystem.disableBlend();
    }

    protected boolean shouldShowHighlight(boolean hovered) {
        return (hoverReactive && hovered) || isSelected;
    }

    public boolean trulyHovered() {
        return this.hovered;
    }
}
