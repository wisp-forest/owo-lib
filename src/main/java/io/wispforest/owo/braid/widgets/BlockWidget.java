package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.function.Consumer;

/// A widget that renders a [BlockState] and optionally a [BlockEntity]
public class BlockWidget extends LeafInstanceWidget {

    public final BlockState blockState;
    public final @Nullable BlockEntity blockEntity;
    public final @Nullable Consumer<MatrixStack> transform;

    public BlockWidget(BlockState blockState, @Nullable BlockEntity blockEntity, @Nullable Consumer<MatrixStack> transform) {
        this.blockState = blockState;
        this.blockEntity = blockEntity;
//        this.transform = matrixStack -> matrixStack.translate(0, Math.sin(System.currentTimeMillis() / 1000d), 0);
        this.transform = transform;
    }

    public BlockWidget(BlockState blockState, @Nullable BlockEntity blockEntity) {
        this(blockState, blockEntity, null);
    }

    public BlockWidget(BlockState blockState, Consumer<MatrixStack> transform) {
        this(blockState, null, transform);
    }

    public BlockWidget(BlockState blockState) {
        this(blockState, null, null);
    }



    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<BlockWidget> {

        public static final Size DEFAULT_SIZE = Size.square(16);

        public Instance(BlockWidget widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var size = DEFAULT_SIZE.constrained(constraints);
            this.transform.setSize(size);
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return 16;
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return 16;
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.empty();
        }

        @Override
        @SuppressWarnings("NonAsciiCharacters")
        public void draw(BraidDrawContext ctx) {
            var client = this.host().client();
            var matrices = ctx.getMatrices();

            matrices.push();

            matrices.translate(this.transform.width() / 2f, this.transform.height() / 2f, 100);

            matrices.scale(40 * (float) (this.transform.width() / 64f), -40 * (float) (this.transform.height() / 64f), 40);

            if (this.widget.transform != null) {
                this.widget.transform.accept(matrices);
            } else {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45 + 180));
            }

            matrices.translate(-.5, -.5, -.5);

            final var vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
            if (this.widget.blockState.getRenderType() != BlockRenderType.INVISIBLE) {
                client.getBlockRenderManager().renderBlockAsEntity(
                    this.widget.blockState,
                    matrices,
                    vertexConsumers,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE,
                    OverlayTexture.DEFAULT_UV
                );
            }

            if (this.widget.blockEntity != null) {
                var медведь = client.getBlockEntityRenderDispatcher().get(this.widget.blockEntity);
                if (медведь != null) {
                    медведь.render(
                        this.widget.blockEntity,
                        0f,
                        matrices,
                        vertexConsumers,
                        LightmapTextureManager.MAX_LIGHT_COORDINATE,
                        OverlayTexture.DEFAULT_UV
                    );
                }
            }

            //TODO: apply lighting from widget.blockState.getLuminance()?
            DiffuseLighting.disableGuiDepthLighting();
            vertexConsumers.draw();
            DiffuseLighting.enableGuiDepthLighting();

            matrices.pop();
        }
    }

    //TODO: idk what prepareBlockEntity is for in BlockComponent
}
