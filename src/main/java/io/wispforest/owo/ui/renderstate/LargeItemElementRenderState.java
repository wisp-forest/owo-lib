package io.wispforest.owo.ui.renderstate;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

public record LargeItemElementRenderState(
    ItemRenderState item,
    ScreenRect bounds,
    ScreenRect scissorArea
) implements OwoSpecialElementRenderState<LargeItemElementRenderState> {

    @Override
    public SpecialGuiElementRenderer<LargeItemElementRenderState> createRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
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

    public static class Renderer extends SpecialGuiElementRenderer<LargeItemElementRenderState> {

        public Renderer(VertexConsumerProvider.Immediate vertexConsumers) {
            super(vertexConsumers);
        }

        @Override
        public Class<LargeItemElementRenderState> getElementClass() {
            return LargeItemElementRenderState.class;
        }

        @Override
        protected void render(LargeItemElementRenderState state, MatrixStack matrices) {
            matrices.scale(state.bounds.width(), -state.bounds.height(), -Math.min(state.bounds.width(), state.bounds.height()));

            var notSideLit = !state.item.isSideLit();
            if (notSideLit) {
                MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ITEMS_FLAT);
            } else {
                MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ITEMS_3D);
            }

            state.item.render(matrices, this.vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
        }

        @Override
        protected float getYOffset(int height, int windowScaleFactor) {
            return height / 2f;
        }

        @Override
        protected String getName() {
            return "owo-ui_large_item";
        }
    }
}
