package io.wispforest.owo.itemgroup.gui;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

@ApiStatus.Internal
public class ItemGroupButtonWidget extends Button {

    public boolean isSelected = false;
    private final OwoItemGroup.ButtonDefinition definition;
    private final int baseU;

    public ItemGroupButtonWidget(int x, int y, int baseU, OwoItemGroup.ButtonDefinition definition, Consumer<ItemGroupButtonWidget> onPress) {
        super(x, y, 24, 24, definition.tooltip(), button -> onPress.accept((ItemGroupButtonWidget) button), Button.DEFAULT_NARRATION);
        this.baseU = baseU;
        this.definition = definition;
    }

    @Override
    public void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.blit(RenderPipelines.GUI_TEXTURED, this.definition.texture(), this.getX(), this.getY(), this.baseU, this.isHoveredOrFocused() || this.isSelected ? this.height : 0, this.width, this.height, 64, 64);

        this.definition.icon().render(context, this.getX() + 4, this.getY() + 4, mouseX, mouseY, delta);
    }

    public boolean isTab() {
        return this.definition instanceof ItemGroupTab;
    }

    public boolean trulyHovered() {
        return this.isHovered;
    }
}
