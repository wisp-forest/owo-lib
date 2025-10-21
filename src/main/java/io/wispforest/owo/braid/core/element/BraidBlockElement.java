package io.wispforest.owo.braid.core.element;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

public record BraidBlockElement(
    BlockState block,
    @Nullable BlockEntityRenderState entity,
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

    public static class Renderer extends SpecialGuiElementRenderer<BraidBlockElement> {

        public Renderer(VertexConsumerProvider.Immediate vertexConsumers) {
            super(vertexConsumers);
        }

        @Override
        public Class<BraidBlockElement> getElementClass() {
            return BraidBlockElement.class;
        }

        @Override
        @SuppressWarnings("NonAsciiCharacters")
        protected void render(BraidBlockElement state, MatrixStack matrices) {
            MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);

            matrices.multiplyPositionMatrix(state.transform);

            if (state.block.getRenderType() != BlockRenderType.INVISIBLE) {
                MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
                    state.block, matrices, vertexConsumers,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV
                );
            }

            if (state.entity != null) {
                var медведь = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().getByRenderState(state.entity);
                if (медведь != null) {
                    var dispatcher = MinecraftClient.getInstance().gameRenderer.getEntityRenderDispatcher();
                    медведь.render(state.entity, matrices, dispatcher.getQueue(), new CameraRenderState());
                    dispatcher.render();
                }
            }
        }

        @Override
        protected float getYOffset(int height, int windowScaleFactor) {
            return height / 2f;
        }

        @Override
        protected String getName() {
            return "owo-ui_block";
        }
    }
}
