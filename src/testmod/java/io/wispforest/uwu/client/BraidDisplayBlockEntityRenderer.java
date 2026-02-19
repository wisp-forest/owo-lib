package io.wispforest.uwu.client;

import io.wispforest.owo.braid.display.BraidDisplay;
import io.wispforest.owo.braid.display.BraidDisplayBinding;
import io.wispforest.owo.braid.display.DisplayQuad;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.uwu.block.BraidDisplayBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicBoolean;

public class BraidDisplayBlockEntityRenderer implements BlockEntityRenderer<BraidDisplayBlockEntity, BraidDisplayBlockEntityRenderer.BraidDisplayBlockEntityRenderState> {

    public BraidDisplayBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public BraidDisplayBlockEntityRenderState createRenderState() {
        return new BraidDisplayBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(BraidDisplayBlockEntity entity, BraidDisplayBlockEntityRenderState state, float tickProgress, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(entity, state, tickProgress, cameraPos, crumblingOverlay);

        if (entity.display == null) {
            entity.disposed = new AtomicBoolean();
            entity.display = new BraidDisplay(
                new DisplayQuad(
                    Vec3.atLowerCornerOf(entity.getBlockPos()).add(1 / 16d, 2 / 16d + 1e-5, 1 - 1 / 16d),
                    new Vec3(0, 0, -14 / 16d),
                    new Vec3(14 / 16d, 0, 0)
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
    public void submit(BraidDisplayBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        var display = state.display;
        if (display.surface.texture() == null) return;

        matrices.translate(
            display.quad.pos.subtract(Vec3.atLowerCornerOf(state.blockPos)).add(0, 1e-4, 0)
        );

        display.render(matrices, queue, state.lightCoords);
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

            Minecraft.getInstance().schedule(() -> {
                this.display.app.dispose();
                BraidDisplayBinding.deactivate(this.display);
            });
        }
    }
}
