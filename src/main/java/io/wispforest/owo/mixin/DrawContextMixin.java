package io.wispforest.owo.mixin;

import com.mojang.blaze3d.vertex.MatrixStack;
import io.wispforest.owo.ui.util.MatrixStackTransformer;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphics.class)
public abstract class DrawContextMixin implements MatrixStackTransformer {

    @Shadow public abstract MatrixStack getMatrices();

    @Override
    public MatrixStack getMatrixStack() {
        return getMatrices();
    }
}
