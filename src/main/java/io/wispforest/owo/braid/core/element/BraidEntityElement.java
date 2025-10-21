package io.wispforest.owo.braid.core.element;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
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
    ScreenRect scissorArea
) implements SpecialGuiElementRenderState {

    @Override
    public int x1() {
        return 0;
    }

    @Override
    public int x2() {
        return (int) this.width;
    }

    @Override
    public int y1() {
        return 0;
    }

    @Override
    public int y2() {
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
    public @Nullable ScreenRect scissorArea() {
        return this.scissorArea;
    }

    @Override
    public @Nullable ScreenRect bounds() {
        var bounds = new ScreenRect(0, 0, (int) this.width, (int) this.height).transformEachVertex(this.pose);

        return this.scissorArea != null
            ? this.scissorArea.intersection(bounds)
            : bounds;
    }

    public static class Renderer extends SpecialGuiElementRenderer<BraidEntityElement> {

        private final EntityRenderManager renderManager = MinecraftClient.getInstance().getEntityRenderDispatcher();

        public Renderer(VertexConsumerProvider.Immediate vertexConsumers) {
            super(vertexConsumers);
        }

        @Override
        public Class<BraidEntityElement> getElementClass() {
            return BraidEntityElement.class;
        }

        @Override
        protected void render(BraidEntityElement state, MatrixStack matrices) {
            MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);

            matrices.multiplyPositionMatrix(state.transform);

            var camera = new CameraRenderState();
            camera.orientation = state.transform.invert().getUnnormalizedRotation(new Quaternionf());

            var dispatcher = MinecraftClient.getInstance().gameRenderer.getEntityRenderDispatcher();
            this.renderManager.render(state.entityState, camera, 0, 0, 0, matrices, dispatcher.getQueue());
            dispatcher.render();
        }

        @Override
        protected float getYOffset(int height, int windowScaleFactor) {
            return 0;
        }

        @Override
        protected String getName() {
            return "owo-entity";
        }
    }
}
