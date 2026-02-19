package io.wispforest.owo.ui.renderstate;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PanoramaRenderer;
import org.jetbrains.annotations.Nullable;

public record CubeMapElementRenderState(
    PanoramaRenderer cubeMap,
    boolean rotate,
    ScreenRectangle bounds,
    ScreenRectangle scissorArea
) implements PictureInPictureRenderState {

    public static OutputOverride outputOverride = null;

    @Override
    public int x0() {
        return this.bounds.left();
    }

    @Override
    public int x1() {
        return this.bounds.right();
    }

    @Override
    public int y0() {
        return this.bounds.top();
    }

    @Override
    public int y1() {
        return this.bounds.bottom();
    }

    @Override
    public float scale() {
        return 1;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return this.scissorArea != null ? this.scissorArea.intersection(this.bounds) : this.bounds;
    }

    public static class Renderer extends PictureInPictureRenderer<CubeMapElementRenderState> {

        private static GuiGraphics dummyContext;

        protected Renderer(MultiBufferSource.BufferSource vertexConsumers) {
            super(vertexConsumers);
        }

        @Override
        public Class<CubeMapElementRenderState> getRenderStateClass() {
            return CubeMapElementRenderState.class;
        }

        @Override
        protected void renderToTexture(CubeMapElementRenderState state, PoseStack matrices) {
            if (dummyContext == null) {
                dummyContext = new GuiGraphics(Minecraft.getInstance(), new GuiRenderState(), 0, 0);
            }

            dummyContext.guiRenderState.reset();

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
        protected String getTextureLabel() {
            return "owo-ui_cubemap";
        }
    }

    public record OutputOverride(GpuTextureView color, GpuTextureView depth, int resetColor) {}
}
