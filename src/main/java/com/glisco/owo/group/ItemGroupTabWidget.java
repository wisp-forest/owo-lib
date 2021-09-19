package com.glisco.owo.group;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class ItemGroupTabWidget extends ButtonWidget {
    public boolean isSelected = false;
    public final boolean flipped;
    private final ItemGroupTab tab;

    public ItemGroupTabWidget(int x, int y, boolean flipped, ItemGroupTab tab, PressAction onPress) {
        super(x, y, 33, 28, new TranslatableText(tab.getTranslationKey()), onPress);
        this.flipped = flipped;
        this.tab = tab;
    }

    protected int getXImage(boolean isHovered) {
        return isSelected ? 2 : (isHovered ? 1 : 0);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, tab.texture());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int xOffset = this.getXImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.drawTexture(matrixStack, this.x, this.y, xOffset * width + (flipped ? width * 3 : 0), 28, this.width, this.height);
        this.renderBackground(matrixStack, minecraftClient, mouseX, mouseY);
        int xIconOffset = flipped ? (this.isHovered() || isSelected ? 10 : 7) : (this.isHovered() || isSelected ? 7 : 10);
        minecraftClient.getItemRenderer().renderGuiItemIcon(tab.icon(), this.x + xIconOffset, this.y + 6);
    }
}
