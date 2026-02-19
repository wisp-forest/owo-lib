package io.wispforest.owo.braid.core.element;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4fc;

public record BraidItemElement(
    ItemStackRenderState item,
    double width,
    double height,
    ScreenRectangle scissorArea,
    Matrix4fc transform,
    Matrix3x2f pose
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

    public static class Renderer extends PictureInPictureRenderer<BraidItemElement> {

        public Renderer(MultiBufferSource.BufferSource vertexConsumers) {
            super(vertexConsumers);
        }

        @Override
        public Class<BraidItemElement> getRenderStateClass() {
            return BraidItemElement.class;
        }

        @Override
        protected void renderToTexture(BraidItemElement state, PoseStack matrices) {
            matrices.scale((float) state.width, (float) -state.height, (float) -Math.min(state.width, state.height));
            matrices.mulPose(state.transform);

            var notSideLit = !state.item.usesBlockLight();
            if (notSideLit) {
                Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
            } else {
                Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
            }

            var dispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
            state.item.submit(matrices, dispatcher.getSubmitNodeStorage(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            dispatcher.renderAllFeatures();
        }

        @Override
        protected float getTranslateY(int height, int windowScaleFactor) {
            return height / 2f;
        }

        @Override
        protected String getTextureLabel() {
            return "owo-item";
        }
    }
}
