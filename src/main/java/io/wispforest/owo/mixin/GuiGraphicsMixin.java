package io.wispforest.owo.mixin;

import io.wispforest.owo.ui.util.MatrixStackTransformer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsMixin implements MatrixStackTransformer {

    @Shadow public abstract Matrix3x2fStack pose();

    @Override
    public Matrix3x2fStack getMatrixStack() {
        return this.pose();
    }
}
