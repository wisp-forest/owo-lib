package io.wispforest.owo.braid.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.util.BraidGuiRenderer;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class TextureSurface implements Surface {

    private final SimpleFramebuffer framebuffer;
    private final EventStream<ResizeCallback> resizeEvents = ResizeCallback.newStream();

    public final TextureSurfaceTexture registeredTexture;
    public final Identifier registeredTextureId;

    private CursorStyle currentCursorStyle = CursorStyle.NONE;

    public final BraidGuiRenderer guiRenderer;

    public TextureSurface(int width, int height) {
        this.framebuffer = new SimpleFramebuffer("texture surface", width, height, true);
        this.guiRenderer = new BraidGuiRenderer(MinecraftClient.getInstance());

        this.registeredTexture = new TextureSurfaceTexture();
        this.registeredTextureId = Owo.id("texture_surface_" + UUID.randomUUID());

        MinecraftClient.getInstance().getTextureManager().registerTexture(this.registeredTextureId, this.registeredTexture);
    }

    public void resize(int width, int height) {
        this.framebuffer.resize(width, height);
        this.resizeEvents.sink().onResize(width, height);

        this.registeredTexture.sync();
    }

    public GpuTextureView texture() {
        return this.framebuffer.getColorAttachmentView();
    }

    @Override
    public int width() {
        return this.framebuffer.textureWidth;
    }

    @Override
    public int height() {
        return this.framebuffer.textureHeight;
    }

    @Override
    public double scaleFactor() {
        return 1;
    }

    @Override
    public EventSource<ResizeCallback> onResize() {
        return this.resizeEvents.source();
    }

    @Override
    public CursorStyle currentCursorStyle() {
        return this.currentCursorStyle;
    }

    @Override
    public void setCursorStyle(CursorStyle style) {
        this.currentCursorStyle = style;
    }

    // ---

    @Override
    public void beginRendering() {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
            this.framebuffer.getColorAttachment(),
            0x00000000,
            this.framebuffer.getDepthAttachment(),
            1
        );
    }

    @Override
    public void endRendering() {
        this.guiRenderer.render(new BraidGuiRenderer.Target(
            this.framebuffer,
            this
        ));
    }

    @Override
    public void dispose() {
        this.framebuffer.delete();
        MinecraftClient.getInstance().getTextureManager().destroyTexture(this.registeredTextureId);
    }

    // ---

    public class TextureSurfaceTexture extends AbstractTexture {

        public TextureSurfaceTexture() {
            this.sync();
            this.sampler = RenderSystem.getSamplerCache().get(FilterMode.NEAREST);
        }

        private void sync() {
             this.glTexture = TextureSurface.this.framebuffer.getColorAttachment();
             this.glTextureView = TextureSurface.this.framebuffer.getColorAttachmentView();
        }

        @Override
        public void close() {}
    }
}
