package io.wispforest.owo.ui.renderstate;

import com.google.common.collect.MapMaker;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import io.wispforest.owo.ui.core.OwoUIPipelines;
import io.wispforest.owo.ui.event.ClientRenderCallback;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.DynamicUniformStorage;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2i;

import java.nio.ByteBuffer;
import java.util.Map;

public record BlurQuadElementRenderState(
    RenderPipeline pipeline,
    Matrix3x2f pose,
    ScreenRect bounds,
    ScreenRect scissorArea,
    TextureSetup textureSetup
) implements SimpleGuiElementRenderState {

    public static Uniforms uniforms;
    public static Framebuffer input;
    public static GpuTextureView inputView;

    @ApiStatus.Internal
    public static void initialize(MinecraftClient client) {
        uniforms = new Uniforms();

        var window = client.getWindow();

        input = new SimpleFramebuffer("owo_blur_input", window.getFramebufferWidth(), window.getFramebufferHeight(), false);
        inputView = RenderSystem.getDevice().createTextureView(input.getColorAttachment());

        WindowResizeCallback.EVENT.register((innerClient, innerWindow) -> {
            if (input == null) return;
            input.resize(innerWindow.getFramebufferWidth(), innerWindow.getFramebufferHeight());

            inputView.close();
            inputView = RenderSystem.getDevice().createTextureView(input.getColorAttachment());
        });

        ClientRenderCallback.AFTER.register($ -> {
            uniforms.clear();
        });
    }

    @ApiStatus.Internal
    public BlurQuadElementRenderState {}

    public BlurQuadElementRenderState(Matrix3x2f pose, ScreenRect bounds, ScreenRect scissorArea, int directions, float quality, float size) {
        this(OwoUIPipelines.GUI_BLUR, pose, bounds, scissorArea, createTextureSetup(directions, quality, size));
    }

    @Override
    public void setupVertices(VertexConsumer vertices) {
        vertices.vertex(this.pose(), (float) this.bounds.getLeft(), (float) this.bounds.getTop());
        vertices.vertex(this.pose(), (float) this.bounds.getLeft(), (float) this.bounds.getBottom());
        vertices.vertex(this.pose(), (float) this.bounds.getRight(), (float) this.bounds.getBottom());
        vertices.vertex(this.pose(), (float) this.bounds.getRight(), (float) this.bounds.getTop());
    }

    @Override
    public RenderPipeline pipeline() {
        return this.pipeline;
    }

    @Override
    public TextureSetup textureSetup() {
        return this.textureSetup;
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return this.scissorArea;
    }

    @Override
    public @Nullable ScreenRect bounds() {
        return this.scissorArea != null ? this.scissorArea.intersection(this.bounds) : this.bounds;
    }

    // ---

    private static final Map<TextureSetup, BlurSetup> blurSetups = new MapMaker().weakKeys().makeMap();

    public static boolean hasBlurSetupFor(TextureSetup textureSetup) {
        return blurSetups.containsKey(textureSetup);
    }

    public static @Nullable BlurSetup getBlurSetupOf(TextureSetup textureSetup) {
        return blurSetups.get(textureSetup);
    }

    private static TextureSetup createTextureSetup(int directions, float quality, float size) {
        var setup = TextureSetup.withoutGlTexture(null);
        blurSetups.put(setup, new BlurSetup(directions, quality, size));
        return setup;
    }

    public record BlurSetup(int directions, float quality, float size) {}

    // ---

    public static class Uniforms {
        public static final int SIZE = new Std140SizeCalculator().putVec2().putFloat().putFloat().putFloat().get();
        private final DynamicUniformStorage<Value> storage = new DynamicUniformStorage<>("Blur Settings UBO", SIZE, 4);

        public void clear() {
            this.storage.clear();
        }

        public GpuBufferSlice write(Vector2i inputResolution, int directions, float quality, float size) {
            return this.storage.write(new Value(inputResolution, directions, quality, size));
        }

        @Environment(EnvType.CLIENT)
        public record Value(Vector2i inputResolution, int directions, float quality, float size) implements DynamicUniformStorage.Uploadable {
            @Override
            public void write(ByteBuffer buffer) {
                Std140Builder.intoBuffer(buffer)
                    .putVec2(inputResolution.x, inputResolution.y)
                    .putFloat(this.directions)
                    .putFloat(this.quality)
                    .putFloat(this.size);
            }
        }
    }
}
