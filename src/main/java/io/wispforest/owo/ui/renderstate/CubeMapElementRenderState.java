package io.wispforest.owo.ui.renderstate;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public record CubeMapElementRenderState(
    CubeMap cubeMap,
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

        private static GuiGraphicsExtractor dummyContext;
        private float spin;

        protected Renderer() {}

        @Override
        public Class<CubeMapElementRenderState> getRenderStateClass() {
            return CubeMapElementRenderState.class;
        }

        @Override
        protected void renderToTexture(CubeMapElementRenderState state, PoseStack matrices, SubmitNodeCollector submitNodeCollector) {
            if (dummyContext == null) {
                dummyContext = new GuiGraphicsExtractor(Minecraft.getInstance(), new GuiRenderState(), 0, 0);
            }

            dummyContext.guiRenderState.reset();

            try {
                CubeMapElementRenderState.outputOverride = new OutputOverride(
                    RenderSystem.outputColorTextureOverride,
                    RenderSystem.outputDepthTextureOverride,
                    0xFF000000
                );

                // TODO: we should probably investigate syncing this to the actual panorama
                //  rotation at some point

                Minecraft minecraft = Minecraft.getInstance();
                if (state.rotate()) {
                    float a = minecraft.getDeltaTracker().getRealtimeDeltaTicks();
                    float delta = (float) (a * minecraft.gameRenderer.gameRenderState().optionsRenderState.panoramaSpeed);
                    this.spin = Mth.wrapDegrees(this.spin + delta * 0.1F);
                }

                state.cubeMap.render(10.0F, this.spin);
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
