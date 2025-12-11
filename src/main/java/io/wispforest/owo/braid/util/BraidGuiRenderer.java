package io.wispforest.owo.braid.util;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.Surface;
import io.wispforest.owo.mixin.braid.GameRendererAccessor;
import io.wispforest.owo.mixin.braid.GuiRendererAccessor;
import io.wispforest.owo.util.pond.BraidGuiRendererExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.render.fog.FogRenderer;

import java.util.ArrayList;

public class BraidGuiRenderer extends GuiRenderer {

    private final MinecraftClient client;

    public BraidGuiRenderer(MinecraftClient client) {
        super(
            new GuiRenderState(),
            client.getBufferBuilders().getEntityVertexConsumers(),
            client.gameRenderer.getEntityRenderCommandQueue(),
            client.gameRenderer.getEntityRenderDispatcher(),
            new ArrayList<>(((GuiRendererAccessor) ((GameRendererAccessor) client.gameRenderer).owo$getGuiRenderer()).owo$getSpecialElementRenderers().values())
        );
        this.client = client;
    }

    public DrawContext newDrawContext(double mouseX, double mouseY) {
        this.trySetFabricState();
        return new DrawContext(
            this.client,
            ((GuiRendererAccessor) this).owo$getState(),
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

            var commandQueueField = GuiRenderer.class.getDeclaredField("orderedRenderCommandQueue");
            commandQueueField.setAccessible(true);
            commandQueueField.set(this, this.client.gameRenderer.getEntityRenderCommandQueue());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Owo.LOGGER.warn("Failed to apply braid's Fabric API GuiRendererMixin workaround, there might be crashes with texture and window surfaces");
        } finally {
            this.fabricStateSet = true;
        }
    }

    public void render(Target target) {
        ((BraidGuiRendererExtension) this).owo$setTarget(target);
        this.render(((GameRendererAccessor) this.client.gameRenderer).owo$getFogRenderer().getFogBuffer(FogRenderer.FogType.NONE));
    }

    @Override
    @Deprecated
    public void render(GpuBufferSlice fogBuffer) {
        super.render(fogBuffer);
    }

    public record Target(Framebuffer framebuffer, Surface surface) {}
}
