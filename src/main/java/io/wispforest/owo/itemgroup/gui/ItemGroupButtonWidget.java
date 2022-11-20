package io.wispforest.owo.itemgroup.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ItemGroupButtonWidget extends ButtonWidget {

    public boolean isSelected = false;
    private final OwoItemGroup.ButtonDefinition definition;
    private final boolean hoverReactive;

    public ItemGroupButtonWidget(int x, int y, boolean hoverReactive, OwoItemGroup.ButtonDefinition definition, String groupTranslationKey, PressAction onPress) {
        super(x, y, 24, 24, Text.translatable(definition.getTranslationKey(groupTranslationKey)), onPress);
        this.definition = definition;
        this.hoverReactive = hoverReactive;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        var client = MinecraftClient.getInstance();

        RenderSystem.setShaderTexture(0, definition.texture());
        drawTexture(matrixStack, this.x, this.y, 0, (shouldShowHighlight(hovered) ? 1 : 0) * height, this.width, this.height, 64, 64);
        this.renderBackground(matrixStack, client, mouseX, mouseY);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        this.definition.icon().render(matrixStack, this.x + 4, this.y + 4, mouseX, mouseY, delta);

        RenderSystem.disableBlend();
    }

    protected boolean shouldShowHighlight(boolean hovered) {
        return (hoverReactive && hovered) || isSelected;
    }

    public boolean trulyHovered() {
        return this.hovered;
    }
}
