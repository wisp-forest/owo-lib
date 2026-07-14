package io.wispforest.owo.braid.core.element;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

public record BraidBlockElement(
    BlockState block,
    @Nullable BlockEntityRenderState entity,
    Matrix4f transform,
    Matrix3x2f pose,
    double width,
    double height,
    ScreenRectangle scissorArea
) implements PictureInPictureRenderState {

    @Override
    public int x0() {
        return 0;
    }

    @Override
    public int x1() {
        return (int) this.width;
    }

    @Override
    public int y0() {
        return 0;
    }

    @Override
    public int y1() {
        return (int) this.height;
    }

    @Override
    public float scale() {
        return 1;
    }

    @Override
    public Matrix3x2f pose() {
        return this.pose;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        var bounds = new ScreenRectangle(0, 0, (int) this.width, (int) this.height).transformMaxBounds(this.pose);

        return this.scissorArea != null
            ? this.scissorArea.intersection(bounds)
            : bounds;
    }

    public static class Renderer extends PictureInPictureRenderer<BraidBlockElement> {

        public Renderer() {}

        @Override
        public Class<BraidBlockElement> getRenderStateClass() {
            return BraidBlockElement.class;
        }

        @Override
        @SuppressWarnings("NonAsciiCharacters")
        protected void renderToTexture(BraidBlockElement state, PoseStack matrices, SubmitNodeCollector submitNodeCollector) {
            Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);

            matrices.mulPose(state.transform);

            if (state.block.getRenderShape() != RenderShape.INVISIBLE) {
                var blockModelRenderState = new BlockModelRenderState();
                Minecraft.getInstance().getModelManager().getBlockModelSet().get(state.block).update(
                    blockModelRenderState,
                    state.block,
                    BlockDisplayContext.create(),
                    67
                );

                blockModelRenderState.submit(matrices, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            }

            if (state.entity != null) {
                var медведь = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(state.entity);
                if (медведь != null) {
                    медведь.submit(state.entity, matrices, submitNodeCollector, new CameraRenderState());
                }
            }
        }

        @Override
        protected float getTranslateY(int height, int windowScaleFactor) {
            return height / 2f;
        }

        @Override
        protected String getTextureLabel() {
            return "owo-ui_block";
        }
    }
}
