package io.wispforest.owo.ui.renderstate;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

public record BlockElementRenderState(
    BlockState state,
    @Nullable BlockEntity entity,
    ScreenRect bounds,
    ScreenRect scissorArea
) implements OwoSpecialElementRenderState<BlockElementRenderState> {

    @Override
    public SpecialGuiElementRenderer<BlockElementRenderState> createRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
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

    public static class Renderer extends SpecialGuiElementRenderer<BlockElementRenderState> {

        public Renderer(VertexConsumerProvider.Immediate vertexConsumers) {
            super(vertexConsumers);
        }

        @Override
        public Class<BlockElementRenderState> getElementClass() {
            return BlockElementRenderState.class;
        }

        @Override
        @SuppressWarnings("NonAsciiCharacters")
        protected void render(BlockElementRenderState state, MatrixStack matrices) {
            MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);

            var width = state.bounds.width();
            var height = state.bounds.height();

            matrices.translate(0, -height / 2f, 100);
            matrices.scale(40 * width / 64f, -40 * height / 64f, -40);

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45 + 180));

            matrices.translate(-.5, -.5, -.5);

            if (state.state.getRenderType() != BlockRenderType.INVISIBLE) {
                MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
                    state.state, matrices, vertexConsumers,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV
                );
            }

            if (state.entity != null) {
                var медведь = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().get(state.entity);
                if (медведь != null) {
                    медведь.render(state.entity, MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false), matrices, vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, MinecraftClient.getInstance().gameRenderer.getCamera().getPos());
                }
            }
        }

        @Override
        protected String getName() {
            return "owo-ui_block";
        }
    }
}
