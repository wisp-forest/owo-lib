package io.wispforest.uwu.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.EventBuffer;
import io.wispforest.owo.braid.core.TextureSurface;
import io.wispforest.owo.braid.core.events.MouseMoveEvent;
import io.wispforest.owo.braid.quads.Ray;
import io.wispforest.owo.braid.quads.WorldQuad;
import io.wispforest.uwu.block.BraidDisplayBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.lang.ref.Cleaner;
import java.util.function.Function;

public class BraidDisplayBlockEntityRenderer implements BlockEntityRenderer<BraidDisplayBlockEntity> {

    private static final Function<TextureSurface, RenderLayer> DISPLAY_LAYER = surface -> RenderLayer.of(
        "uwu:braid_display",
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        16384,
        RenderLayer.MultiPhaseParameters.builder()
            .texture(new SurfaceTexture(surface))
            .program(RenderPhase.ENTITY_SOLID_PROGRAM)
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .overlay(RenderPhase.DISABLE_OVERLAY_COLOR)
            .build(false)
    );

    public BraidDisplayBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(BraidDisplayBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var client = MinecraftClient.getInstance();

        if (entity.app == null) {
            entity.app = new AppState(
                null,
                client,
                new TextureSurface(128, 128),
                new EventBuffer(),
                new BraidDisplayBlockEntity.Provider(
                    entity,
                    new BraidDisplayBlockEntity.App()
                )
            );

            APP_CLEANER.register(entity, new AppCleanCallback(entity.app));
        }

        var app = entity.app;

        var quad = new WorldQuad(
            Vec3d.of(entity.getPos()).add(0, 1 / 16d, 1),
            new Vec3d(0, 0, -1),
            new Vec3d(1, 0, 0)
        );

        var ray = new Ray(
            client.player.getEyePos(),
            client.player.getRotationVec(1),
            Double.POSITIVE_INFINITY
        );

        var intersect = quad.intersect(ray);
        if (intersect != null) {
            var cursorX = intersect.x() * app.surface.width();
            var cursorY = intersect.y() * app.surface.height();

            var deltaX = cursorX - entity.cursorX;
            var deltaY = cursorY - entity.cursorY;
            entity.cursorX = cursorX;
            entity.cursorY = cursorY;

            if (deltaX != 0 || deltaY != 0) {
                app.eventBuffer.add(new MouseMoveEvent(cursorX, cursorY, deltaX, deltaY));
            }
        }

        app.updateWidgetsAndInteractions(
            client.getRenderTickCounter().getTickDelta(false),
            client.getRenderTickCounter().getLastFrameDuration()
        );

        var immediate = client.getBufferBuilders().getEntityVertexConsumers();
        immediate.draw();
        app.draw(new DrawContext(client, immediate));

        // ---

        var surface = (TextureSurface) app.surface;
        var layer = DISPLAY_LAYER.apply(surface);

        matrices.translate(.5, 0, .5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
        matrices.translate(-.5, 0, -.5);

        matrices.translate(0, 1 / 16f, 0);

        var buffer = vertexConsumers.getBuffer(layer);
        var matrix = matrices.peek().getPositionMatrix();
        buffer.vertex(matrix, 0, 1e-4f, 0).color(1f, 1f, 1f, 1f).texture(1, 0).overlay(overlay).light(light).normal(0, 1, 0);
        buffer.vertex(matrix, 0, 1e-4f, 1).color(1f, 1f, 1f, 1f).texture(1, 1).overlay(overlay).light(light).normal(0, 1, 0);
        buffer.vertex(matrix, 1, 1e-4f, 1).color(1f, 1f, 1f, 1f).texture(0, 1).overlay(overlay).light(light).normal(0, 1, 0);
        buffer.vertex(matrix, 1, 1e-4f, 0).color(1f, 1f, 1f, 1f).texture(0, 0).overlay(overlay).light(light).normal(0, 1, 0);
    }

    // ---

    private static final Cleaner APP_CLEANER = Cleaner.create();

    private record AppCleanCallback(AppState app) implements Runnable {
        @Override
        public void run() {
            this.app.dispose();
        }
    }

    // ---

    private static class SurfaceTexture extends RenderPhase.TextureBase {
        public SurfaceTexture(TextureSurface surface) {
            super(
                () -> RenderSystem.setShaderTexture(0, surface.texture()),
                () -> {}
            );
        }
    }
}
