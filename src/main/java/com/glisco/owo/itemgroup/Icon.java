package com.glisco.owo.itemgroup;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * An icon used for rendering on buttons in {@link OwoItemGroup}s
 * <p>
 * Default implementations provided for textures and itemstacks
 */
@SuppressWarnings("ClassCanBeRecord")
public interface Icon {

    void render(MatrixStack matrixStack, int x, int y, int mouseX, int mouseY, float delta);

    static Icon of(ItemStack stack) {
        return new ItemIcon(stack);
    }

    static Icon of(ItemConvertible item) {
        return of(new ItemStack(item));
    }

    static Icon of(Identifier texture, int u, int v, int textureWidth, int textureHeight) {
        return new TextureIcon(texture, u, v, textureWidth, textureHeight);
    }

    /**
     * Renders an {@link ItemStack}
     */
    @ApiStatus.Internal
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

    /**
     * Renders a 16x16 region of the given texture, starting at (u, v)
     */
    @ApiStatus.Internal
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
