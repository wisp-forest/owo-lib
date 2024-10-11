package io.wispforest.owo.itemgroup.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

@ApiStatus.Internal
public class ItemGroupButtonWidget extends ButtonWidget {

    public boolean isSelected = false;
    private final OwoItemGroup.ButtonDefinition definition;
    private final int baseU;

    public ItemGroupButtonWidget(int x, int y, int baseU, OwoItemGroup.ButtonDefinition definition, Consumer<ItemGroupButtonWidget> onPress) {
        super(x, y, 24, 24, definition.tooltip(), button -> onPress.accept((ItemGroupButtonWidget) button), ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.baseU = baseU;
        this.definition = definition;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.enableDepthTest();
        context.drawTexture(RenderLayer::getGuiTextured, this.definition.texture(), this.getX(), this.getY(), this.baseU, this.isSelected() || this.isSelected ? this.height : 0, this.width, this.height, 64, 64);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        this.definition.icon().render(context, this.getX() + 4, this.getY() + 4, mouseX, mouseY, delta);
        RenderSystem.disableBlend();
    }

    public boolean isTab() {
        return this.definition instanceof ItemGroupTab;
    }

    public boolean trulyHovered() {
        return this.hovered;
    }
}
