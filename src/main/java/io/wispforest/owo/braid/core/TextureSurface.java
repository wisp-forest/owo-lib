package io.wispforest.owo.braid.core;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.util.BraidGuiRenderer;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public class TextureSurface implements Surface {

    private final TextureTarget target;
    private final EventStream<ResizeCallback> resizeEvents = ResizeCallback.newStream();

    public final TextureSurfaceTexture registeredTexture;
    public final Identifier registeredTextureId;

    private CursorStyle currentCursorStyle = CursorStyle.NONE;

    public final BraidGuiRenderer guiRenderer;

    public TextureSurface(int width, int height) {
        this.target = new TextureTarget("texture surface", width, height, true);
        this.guiRenderer = new BraidGuiRenderer(Minecraft.getInstance());

        this.registeredTexture = new TextureSurfaceTexture();
        this.registeredTextureId = Owo.id("texture_surface_" + UUID.randomUUID());

        Minecraft.getInstance().getTextureManager().register(this.registeredTextureId, this.registeredTexture);
    }

    public void resize(int width, int height) {
        this.target.resize(width, height);
        this.resizeEvents.sink().onResize(width, height);

        this.registeredTexture.sync();
    }

    public GpuTextureView texture() {
        return this.target.getColorTextureView();
    }

    @Override
    public int width() {
        return this.target.width;
    }

    @Override
    public int height() {
        return this.target.height;
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
            this.target.getColorTexture(),
            0x00000000,
            this.target.getDepthTexture(),
            1
        );
    }

    @Override
    public void endRendering() {
        this.guiRenderer.render(new BraidGuiRenderer.Target(
            this.target,
            this
        ));
    }

    @Override
    public void dispose() {
        this.target.destroyBuffers();
        Minecraft.getInstance().getTextureManager().release(this.registeredTextureId);
    }

    // ---

    public class TextureSurfaceTexture extends AbstractTexture {

        public TextureSurfaceTexture() {
            this.sync();
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        }

        private void sync() {
             this.texture = TextureSurface.this.target.getColorTexture();
             this.textureView = TextureSurface.this.target.getColorTextureView();
        }

        @Override
        public void close() {}
    }
}
