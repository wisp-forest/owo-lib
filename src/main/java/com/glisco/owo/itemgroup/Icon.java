package com.glisco.owo.itemgroup;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@SuppressWarnings("ClassCanBeRecord")
public interface Icon {

    void render(MatrixStack matrixStack, int x, int y, int mouseX, int mouseY, float delta);

    static Icon of(ItemStack stack) {
        return new ItemIcon(stack);
    }

    static Icon of(ItemConvertible item) {
        return new ItemIcon(new ItemStack(item));
    }

    static Icon of(Identifier texture, int u, int v, int textureWidth, int textureHeight) {
        return new TextureIcon(texture, u, v, textureWidth, textureHeight);
    }

    class ItemIcon implements Icon {

        private final ItemStack stack;

        private ItemIcon(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public void render(MatrixStack matrixStack, int x, int y, int mouseX, int mouseY, float delta) {
            MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(stack, x, y);
        }
    }

    class TextureIcon implements Icon {

        private final Identifier texture;
        private final int u, v;
        private final int textureWidth, textureHeight;

        public TextureIcon(Identifier texture, int u, int v, int textureWidth, int textureHeight) {
            this.texture = texture;
            this.u = u;
            this.v = v;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
        }

        @Override
        public void render(MatrixStack matrixStack, int x, int y, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, texture);
            DrawableHelper.drawTexture(matrixStack, x, y, u, v, 16, 16, textureWidth, textureHeight);
        }
    }

}
