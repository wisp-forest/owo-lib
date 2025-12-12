package io.wispforest.owo.ui.renderstate;

import com.google.common.collect.MapMaker;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.wispforest.owo.ui.core.OwoUIPipelines;
import io.wispforest.owo.ui.event.ClientRenderCallback;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.DynamicUniformStorage;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2i;

import java.nio.ByteBuffer;
import java.util.Map;

public record BlurQuadElementRenderState(
    RenderPipeline pipeline,
    Matrix3x2f pose,
    ScreenRectangle bounds,
    ScreenRectangle scissorArea,
    TextureSetup textureSetup
) implements GuiElementRenderState {

    public static Uniforms uniforms;
    public static RenderTarget input;
    public static GpuTextureView inputView;

    @ApiStatus.Internal
    public static void initialize(Minecraft client) {
        uniforms = new Uniforms();

        var window = client.getWindow();

        input = new TextureTarget("owo_blur_input", window.getWidth(), window.getHeight(), false);
        inputView = RenderSystem.getDevice().createTextureView(input.getColorTexture());

        WindowResizeCallback.EVENT.register((innerClient, innerWindow) -> {
            if (input == null) return;
            input.resize(innerWindow.getWidth(), innerWindow.getHeight());

            inputView.close();
            inputView = RenderSystem.getDevice().createTextureView(input.getColorTexture());
        });

        ClientRenderCallback.AFTER.register($ -> {
            uniforms.clear();
        });
    }

    @ApiStatus.Internal
    public BlurQuadElementRenderState {}

    public BlurQuadElementRenderState(Matrix3x2f pose, ScreenRectangle bounds, ScreenRectangle scissorArea, int directions, float quality, float size) {
        this(OwoUIPipelines.GUI_BLUR, pose, bounds, scissorArea, createTextureSetup(directions, quality, size));
    }

    @Override
    public void buildVertices(VertexConsumer vertices) {
        vertices.addVertexWith2DPose(this.pose(), (float) this.bounds.left(), (float) this.bounds.top());
        vertices.addVertexWith2DPose(this.pose(), (float) this.bounds.left(), (float) this.bounds.bottom());
        vertices.addVertexWith2DPose(this.pose(), (float) this.bounds.right(), (float) this.bounds.bottom());
        vertices.addVertexWith2DPose(this.pose(), (float) this.bounds.right(), (float) this.bounds.top());
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
    public @Nullable ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
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
        var setup = TextureSetup.singleTexture(null, null);
        blurSetups.put(setup, new BlurSetup(directions, quality, size));
        return setup;
    }

    public record BlurSetup(int directions, float quality, float size) {}

    // ---

    public static class Uniforms {
        public static final int SIZE = new Std140SizeCalculator().putVec2().putFloat().putFloat().putFloat().get();
        private final DynamicUniformStorage<Value> storage = new DynamicUniformStorage<>("Blur Settings UBO", SIZE, 4);

        public void clear() {
            this.storage.endFrame();
        }

        public GpuBufferSlice write(Vector2i inputResolution, int directions, float quality, float size) {
            return this.storage.writeUniform(new Value(inputResolution, directions, quality, size));
        }

        @Environment(EnvType.CLIENT)
        public record Value(Vector2i inputResolution, int directions, float quality, float size) implements DynamicUniformStorage.DynamicUniform {
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
