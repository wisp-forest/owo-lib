package io.wispforest.owo.ui.renderstate;

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

public record OwoItemElementRenderState(
    ItemStackRenderState item,
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

    public static class Renderer extends PictureInPictureRenderer<OwoItemElementRenderState> {

        public Renderer(MultiBufferSource.BufferSource vertexConsumers) {
            super(vertexConsumers);
        }

        @Override
        public Class<OwoItemElementRenderState> getRenderStateClass() {
            return OwoItemElementRenderState.class;
        }

        @Override
        protected void renderToTexture(OwoItemElementRenderState state, PoseStack matrices) {
            matrices.scale(state.bounds.width(), -state.bounds.height(), -Math.min(state.bounds.width(), state.bounds.height()));

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
