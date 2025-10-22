package io.wispforest.owo.ui.renderstate;

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
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.service.ITransformer;

public record EntityElementRenderState(
    EntityRenderState entityState,
    Matrix4f transform,
    ScreenRect bounds,
    ScreenRect scissorArea
) implements SpecialGuiElementRenderState {

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

    public static class Renderer extends SpecialGuiElementRenderer<EntityElementRenderState> {

        private final EntityRenderManager renderManager = MinecraftClient.getInstance().getEntityRenderDispatcher();

        protected Renderer(VertexConsumerProvider.Immediate vertexConsumers) {
            super(vertexConsumers);
        }

        @Override
        public Class<EntityElementRenderState> getElementClass() {
            return EntityElementRenderState.class;
        }

        @Override
        protected void render(EntityElementRenderState state, MatrixStack matrices) {
            MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);

            matrices.multiplyPositionMatrix(state.transform);

            var camera = new CameraRenderState();
            camera.orientation = state.transform.invert().getUnnormalizedRotation(new Quaternionf());

            var dispatcher = MinecraftClient.getInstance().gameRenderer.getEntityRenderDispatcher();
            this.renderManager.render(state.entityState, camera, 0, 0, 0, matrices, dispatcher.getQueue());
            dispatcher.render();
        }

        @Override
        protected String getName() {
            return "owo-ui_entity";
        }
    }
}
