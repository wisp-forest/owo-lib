package io.wispforest.owo.braid.core.element;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public record BraidEntityElement(
    EntityRenderState entityState,
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

    public static class Renderer extends PictureInPictureRenderer<BraidEntityElement> {

        private final EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();

        public Renderer(MultiBufferSource.BufferSource vertexConsumers) {
            super(vertexConsumers);
        }

        @Override
        public Class<BraidEntityElement> getRenderStateClass() {
            return BraidEntityElement.class;
        }

        @Override
        protected void renderToTexture(BraidEntityElement state, PoseStack matrices) {
            Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);

            matrices.mulPose(state.transform);

            var camera = new CameraRenderState();
            camera.orientation = state.transform.invert().getUnnormalizedRotation(new Quaternionf());

            var dispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
            this.renderManager.submit(state.entityState, camera, 0, 0, 0, matrices, dispatcher.getSubmitNodeStorage());
            dispatcher.renderAllFeatures();
        }

        @Override
        protected float getTranslateY(int height, int windowScaleFactor) {
            return 0;
        }

        @Override
        protected String getTextureLabel() {
            return "owo-entity";
        }
    }
}
