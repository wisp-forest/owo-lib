package io.wispforest.owo.ui.renderstate;

import io.wispforest.owo.util.pond.OwoEntityRenderDispatcherExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public record EntityElementRenderState(
    EntityRenderState entityState,
    Matrix4f transform,
    ScreenRect bounds,
    ScreenRect scissorArea
) implements OwoSpecialElementRenderState<EntityElementRenderState> {

    @Override
    public SpecialGuiElementRenderer<EntityElementRenderState> createRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        return new Renderer(vertexConsumers);
    }

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

        private final EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();

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

            var dispatcher = (OwoEntityRenderDispatcherExtension) this.dispatcher;
            dispatcher.owo$setCounterRotate(true);

            matrices.multiplyPositionMatrix(state.transform);

            this.dispatcher.setRenderShadows(false);
            this.dispatcher.render(state.entityState, 0, 0, 0, matrices, this.vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            this.dispatcher.setRenderShadows(true);

            dispatcher.owo$setCounterRotate(false);
        }

        @Override
        protected String getName() {
            return "owo-ui_entity";
        }
    }
}
