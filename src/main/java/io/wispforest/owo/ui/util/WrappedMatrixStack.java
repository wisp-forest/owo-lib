package io.wispforest.owo.ui.util;

import net.minecraft.client.util.math.MatrixStack;

public record WrappedMatrixStack(MatrixStack matrixStack) implements MatrixStackTransformer<WrappedMatrixStack> {

    public WrappedMatrixStack() {
        this(new MatrixStack());
    }

    @Override
    public MatrixStack getMatrixStack() {
        return matrixStack();
    }
}
