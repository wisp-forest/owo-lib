package com.glisco.owo.itemgroup.gui;

import com.glisco.owo.itemgroup.OwoItemGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ItemGroupButtonWidget extends ButtonWidget {

    public boolean isSelected = false;
    private final OwoItemGroup.ButtonDefinition definition;
    private final boolean hoverReactive;

    public ItemGroupButtonWidget(int x, int y, boolean hoverReactive, OwoItemGroup.ButtonDefinition definition, String groupTranslationKey, PressAction onPress) {
        super(x, y, 24, 24, new TranslatableText(definition.getTranslationKey(groupTranslationKey)), onPress);
        this.definition = definition;
        this.hoverReactive = hoverReactive;
    }

    protected boolean shouldShowHighlight(boolean hovered) {
        return (hoverReactive && hovered) || isSelected;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();

        RenderSystem.setShaderTexture(0, definition.texture());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();

        drawTexture(matrixStack, this.x, this.y, 0, (shouldShowHighlight(hovered) ? 1 : 0) * height, this.width, this.height, 64, 64);

        this.renderBackground(matrixStack, minecraftClient, mouseX, mouseY);
        this.definition.icon().render(matrixStack, this.x + 4, this.y + 4, mouseX, mouseY, delta);
    }
}
