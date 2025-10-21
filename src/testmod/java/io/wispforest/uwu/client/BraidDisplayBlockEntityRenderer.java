package io.wispforest.uwu.client;

import io.wispforest.owo.braid.display.BraidDisplay;
import io.wispforest.owo.braid.display.BraidDisplayBinding;
import io.wispforest.owo.braid.display.DisplayQuad;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.uwu.block.BraidDisplayBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicBoolean;

public class BraidDisplayBlockEntityRenderer implements BlockEntityRenderer<BraidDisplayBlockEntity, BraidDisplayBlockEntityRenderer.BraidDisplayBlockEntityRenderState> {

    public BraidDisplayBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public BraidDisplayBlockEntityRenderState createRenderState() {
        return new BraidDisplayBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(BraidDisplayBlockEntity entity, BraidDisplayBlockEntityRenderState state, float tickProgress, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickProgress, cameraPos, crumblingOverlay);

        if (entity.display == null) {
            entity.disposed = new AtomicBoolean();
            entity.display = new BraidDisplay(
                new DisplayQuad(
                    Vec3d.of(entity.getPos()).add(1 / 16d, 2 / 16d + 1e-5, 1 - 1 / 16d),
                    new Vec3d(0, 0, -14 / 16d),
                    new Vec3d(14 / 16d, 0, 0)
                ),
                128, 128,
                new BraidDisplayBlockEntity.Provider(
                    entity,
                    new Panel(
                        Panel.VANILLA_LIGHT,
                        new BraidDisplayBlockEntity.App()
                    )
                )
            );

            BraidDisplayBinding.activate(entity.display);
            DISPLAY_CLEANER.register(entity, new DisplayCleanCallback(entity.display, entity.disposed));
        }

        state.display = entity.display;
    }

    @Override
    public void render(BraidDisplayBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        var display = state.display;
        if (display.surface.texture() == null) return;

        matrices.translate(
            display.quad.pos.subtract(Vec3d.of(state.pos)).add(0, 1e-4, 0)
        );

        display.render(matrices, queue, state.lightmapCoordinates);
    }

    public static class BraidDisplayBlockEntityRenderState extends BlockEntityRenderState {
        public BraidDisplay display;
    }

    // ---

    private static final Cleaner DISPLAY_CLEANER = Cleaner.create();

    private record DisplayCleanCallback(BraidDisplay display, AtomicBoolean disposed) implements Runnable {
        @Override
        public void run() {
            if (!this.disposed.compareAndSet(false, true)) return;

            MinecraftClient.getInstance().send(() -> {
                this.display.app.dispose();
                BraidDisplayBinding.deactivate(this.display);
            });
        }
    }
}
