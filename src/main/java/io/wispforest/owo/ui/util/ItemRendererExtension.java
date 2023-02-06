package io.wispforest.owo.ui.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ItemRendererExtension {

    /**
     * Helper method to pass a custom Matrices and Z Offset
     */
    default void renderGuiItemOverlay(MatrixStack matrices, TextRenderer renderer, ItemStack stack, int x, int y, int z) {
        this.renderGuiItemOverlay(matrices, renderer, stack, x, y, z,null);
    }

    /**
     * Helper method to pass a custom Matrices and Z Offset
     */
    void renderGuiItemOverlay(MatrixStack matrices, TextRenderer renderer, ItemStack stack, int x, int y, int z, @Nullable String countLabel);

}
