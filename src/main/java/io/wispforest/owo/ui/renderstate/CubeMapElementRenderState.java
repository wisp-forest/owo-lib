package io.wispforest.owo.ui.renderstate;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

public record CubeMapElementRenderState(
    RotatingCubeMapRenderer cubeMap,
    boolean rotate,
    ScreenRect bounds,
    ScreenRect scissorArea
) implements SpecialGuiElementRenderState {

    public static OutputOverride outputOverride = null;

    @Override
    public int x1() {
        return this.bounds.getLeft();
    }

    @Override
    public int x2() {
        return this.bounds.getRight();
    }

    @Override
    public int y1() {
        return this.bounds.getTop();
    }

    @Override
    public int y2() {
        return this.bounds.getBottom();
    }

    @Override
    public float scale() {
        return 1;
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return this.scissorArea;
    }

    @Override
    public @Nullable ScreenRect bounds() {
        return this.scissorArea != null ? this.scissorArea.intersection(this.bounds) : this.bounds;
    }

    public static class Renderer extends SpecialGuiElementRenderer<CubeMapElementRenderState> {

        private static DrawContext dummyContext;

        protected Renderer(VertexConsumerProvider.Immediate vertexConsumers) {
            super(vertexConsumers);
        }

        @Override
        public Class<CubeMapElementRenderState> getElementClass() {
            return CubeMapElementRenderState.class;
        }

        @Override
        protected void render(CubeMapElementRenderState state, MatrixStack matrices) {
            if (dummyContext == null) {
                dummyContext = new DrawContext(MinecraftClient.getInstance(), new GuiRenderState(), 0, 0);
            }

            dummyContext.state.clear();

            try {
                CubeMapElementRenderState.outputOverride = new OutputOverride(
                    RenderSystem.outputColorTextureOverride,
                    RenderSystem.outputDepthTextureOverride,
                    0xFF000000
                );

                state.cubeMap.render(dummyContext, state.bounds.width(), state.bounds.height(), state.rotate());
            } finally {
                CubeMapElementRenderState.outputOverride = null;
            }
        }

        @Override
        protected String getName() {
            return "owo-ui_cubemap";
        }
    }

    public record OutputOverride(GpuTextureView color, GpuTextureView depth, int resetColor) {}
}
