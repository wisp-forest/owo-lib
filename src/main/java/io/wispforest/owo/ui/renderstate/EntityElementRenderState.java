package io.wispforest.owo.ui.renderstate;

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
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public record EntityElementRenderState(
    EntityRenderState entityState,
    Matrix4f transform,
    ScreenRectangle bounds,
    ScreenRectangle scissorArea
) implements PictureInPictureRenderState {

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

    public static class Renderer extends PictureInPictureRenderer<EntityElementRenderState> {

        private final EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();

        protected Renderer(MultiBufferSource.BufferSource vertexConsumers) {
            super(vertexConsumers);
        }

        @Override
        public Class<EntityElementRenderState> getRenderStateClass() {
            return EntityElementRenderState.class;
        }

        @Override
        protected void renderToTexture(EntityElementRenderState state, PoseStack matrices) {
            Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);

            matrices.mulPose(state.transform);

            var camera = new CameraRenderState();
            camera.orientation = state.transform.invert().getUnnormalizedRotation(new Quaternionf());

            var dispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
            this.renderManager.submit(state.entityState, camera, 0, 0, 0, matrices, dispatcher.getSubmitNodeStorage());
            dispatcher.renderAllFeatures();
        }

        @Override
        protected String getTextureLabel() {
            return "owo-ui_entity";
        }
    }
}
