package io.wispforest.uwu.client;

import io.wispforest.owo.braid.display.BraidDisplay;
import io.wispforest.owo.braid.display.BraidDisplayBinding;
import io.wispforest.owo.braid.display.DisplayQuad;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.uwu.block.BraidDisplayBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicBoolean;

public class BraidDisplayBlockEntityRenderer implements BlockEntityRenderer<BraidDisplayBlockEntity> {

    public BraidDisplayBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(BraidDisplayBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
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

        // ---

        var display = entity.display;

        var offset = display.quad.pos.subtract(Vec3d.of(entity.getPos())).add(0, 1e-4, 0);
        matrices.translate(offset.x, offset.y, offset.z);

        display.render(matrices, vertexConsumers, light);
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
