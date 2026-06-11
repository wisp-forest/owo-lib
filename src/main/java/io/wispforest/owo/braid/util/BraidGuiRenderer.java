package io.wispforest.owo.braid.util;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.Surface;
import io.wispforest.owo.mixin.braid.GameRendererAccessor;
import io.wispforest.owo.mixin.braid.GuiRendererAccessor;
import io.wispforest.owo.neoforge.mixin.neoforge.PictureInPictureRendererPoolAccessor;
import io.wispforest.owo.util.pond.BraidGuiRendererExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

import java.util.ArrayList;

public class BraidGuiRenderer extends GuiRenderer {

    private final Minecraft client;

    public BraidGuiRenderer(Minecraft client) {
        super(
            new GuiRenderState(),
            client.renderBuffers().bufferSource(),
            client.gameRenderer.getSubmitNodeStorage(),
            client.gameRenderer.getFeatureRenderDispatcher(),
            new ArrayList<>(((GuiRendererAccessor) ((GameRendererAccessor) client.gameRenderer).owo$getGuiRenderer()).owo$getPictureInPictureRenderers().values().stream().map(pool -> ((PictureInPictureRendererPoolAccessor<?>) pool).owo$factory()).toList())
        );
        this.client = client;
    }

    public GuiGraphicsExtractor newGraphics(double mouseX, double mouseY) {
        this.trySetFabricState();
        return new GuiGraphicsExtractor(
            this.client,
            ((GuiRendererAccessor) this).owo$getRenderState(),
            (int) mouseX, (int) mouseY
        );
    }

    private boolean fabricStateSet = false;
    private void trySetFabricState() {
        if (this.fabricStateSet) {
            return;
        }

        try {
            var initField = GuiRenderer.class.getDeclaredField("hasFabricInitialized");
            initField.setAccessible(true);
            initField.set(this, true);

            var nodeStorageField = GuiRenderer.class.getDeclaredField("submitNodeStorage");
            nodeStorageField.setAccessible(true);
            nodeStorageField.set(this, this.client.gameRenderer.getSubmitNodeStorage());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Owo.LOGGER.warn("Failed to apply braid's Fabric API GuiRendererMixin workaround, there might be crashes with texture and window surfaces");
        } finally {
            this.fabricStateSet = true;
        }
    }

    public void render(Target target) {
        ((BraidGuiRendererExtension) this).owo$setTarget(target);
        this.render(((GameRendererAccessor) this.client.gameRenderer).owo$getFogRenderer().getBuffer(FogRenderer.FogMode.NONE));
    }

    @Override
    @Deprecated
    public void render(GpuBufferSlice fogBuffer) {
        super.render(fogBuffer);
    }

    public record Target(RenderTarget framebuffer, Surface surface) {}
}
