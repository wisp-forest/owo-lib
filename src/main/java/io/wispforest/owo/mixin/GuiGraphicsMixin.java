package io.wispforest.owo.mixin;

import com.mojang.blaze3d.vertex.MatrixStack;
import io.wispforest.owo.ui.util.MatrixStackTransformer;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin implements MatrixStackTransformer {

    @Override
    @Accessor
    public abstract MatrixStack getMatrixStack();
}
