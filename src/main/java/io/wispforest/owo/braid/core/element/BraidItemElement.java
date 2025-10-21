package io.wispforest.owo.braid.core.element;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4fc;

public record BraidItemElement(
    ItemRenderState item,
    double width,
    double height,
    ScreenRect scissorArea,
    Matrix4fc transform,
    Matrix3x2f pose
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

    public static class Renderer extends SpecialGuiElementRenderer<BraidItemElement> {

        public Renderer(VertexConsumerProvider.Immediate vertexConsumers) {
            super(vertexConsumers);
        }

        @Override
        public Class<BraidItemElement> getElementClass() {
            return BraidItemElement.class;
        }

        @Override
        protected void render(BraidItemElement state, MatrixStack matrices) {
            matrices.scale((float) state.width, (float) -state.height, (float) -Math.min(state.width, state.height));
            matrices.multiplyPositionMatrix(state.transform);

            var notSideLit = !state.item.isSideLit();
            if (notSideLit) {
                MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ITEMS_FLAT);
            } else {
                MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ITEMS_3D);
            }

            var dispatcher = MinecraftClient.getInstance().gameRenderer.getEntityRenderDispatcher();
            state.item.render(matrices, dispatcher.getQueue(), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, 0);
            dispatcher.render();
        }

        @Override
        protected float getYOffset(int height, int windowScaleFactor) {
            return height / 2f;
        }

        @Override
        protected String getName() {
            return "owo-item";
        }
    }
}
