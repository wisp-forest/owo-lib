package io.wispforest.owo.shader;

import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.window.context.CurrentWindowContext;
import io.wispforest.owo.ui.window.context.WindowContext;
import io.wispforest.owo.util.SupportsFeatures;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;

/**
 * A simple GlProgram implementation that applies an approximate gaussian
 * blur to the quads it draws. Look at {@link Surface#blur(float, float)} for a
 * reference implementation
 */
public class BlurProgram extends GlProgram {

    private GlUniform inputResolution;
    private GlUniform directions;
    private GlUniform quality;
    private GlUniform size;

    public BlurProgram() {
        super(Identifier.of("owo", "blur"), VertexFormats.POSITION);
    }

    public void setParameters(int directions, float quality, float size) {
        this.directions.set((float) directions);
        this.size.set(size);
        this.quality.set(quality);
    }

    @Override
    public void use() {
        var window = CurrentWindowContext.current();
        var buffer = window.framebuffer();
        var input = window.get(BlurInputFeature.KEY).input;

        input.beginWrite(false);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer.fbo);
        GL30.glBlitFramebuffer(0, 0, buffer.textureWidth, buffer.textureHeight, 0, 0, buffer.textureWidth, buffer.textureHeight, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR);
        buffer.beginWrite(false);

        this.inputResolution.set((float) buffer.textureWidth, (float) buffer.textureHeight);
        this.backingProgram.addSampler("InputSampler", input.getColorAttachment());

        super.use();
    }

    @Override
    protected void setup() {
        this.inputResolution = this.findUniform("InputResolution");
        this.directions = this.findUniform("Directions");
        this.quality = this.findUniform("Quality");
        this.size = this.findUniform("Size");
    }

    private static final class BlurInputFeature implements AutoCloseable {
        private static final SupportsFeatures.Key<WindowContext, BlurInputFeature> KEY = new SupportsFeatures.Key<>(BlurInputFeature::new);

        private final Framebuffer input;

        public BlurInputFeature(WindowContext ctx) {
            this.input = new SimpleFramebuffer(ctx.framebufferWidth(), ctx.framebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);

            ctx.framebufferResized().subscribe((newWidth, newHeight) -> {
                this.input.resize(newWidth, newHeight, MinecraftClient.IS_SYSTEM_MAC);
            });
        }

        @Override
        public void close() {
            this.input.delete();
        }
    }
}