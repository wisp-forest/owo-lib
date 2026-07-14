package io.wispforest.owo.braid.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.wispforest.owo.braid.core.Surface;
import io.wispforest.owo.mixin.braid.GameRendererAccessor;
import io.wispforest.owo.mixin.braid.GuiRendererAccessor;
import io.wispforest.owo.util.pond.BraidGuiRendererExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

import java.util.ArrayList;

public class BraidGuiRenderer extends GuiRenderer {

    private final Minecraft client;

    public BraidGuiRenderer(Minecraft client) {
        super(
            new GuiRenderState(),
            client.gameRenderer.featureRenderDispatcher(),
            new ArrayList<>(((GuiRendererAccessor) ((GameRendererAccessor) client.gameRenderer).owo$getGuiRenderer()).owo$getPictureInPictureRenderers().values())
        );
        this.client = client;
    }

    public GuiGraphicsExtractor newGraphics(double mouseX, double mouseY) {
        return new GuiGraphicsExtractor(
            this.client,
            ((GuiRendererAccessor) this).owo$getRenderState(),
            (int) mouseX, (int) mouseY
        );
    }

    public void render(Target target) {
        ((BraidGuiRendererExtension) this).owo$setTarget(target);
        super.render();
    }

    public record Target(RenderTarget framebuffer, Surface surface) {}
}
