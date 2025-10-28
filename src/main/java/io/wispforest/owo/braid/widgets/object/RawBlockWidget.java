package io.wispforest.owo.braid.widgets.object;

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
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.OptionalDouble;
import java.util.function.Consumer;

/// A widget that renders a [BlockState] and optionally a [BlockEntity]
public class RawBlockWidget extends LeafInstanceWidget {

    public final BlockState blockState;
    public final @Nullable BlockEntity blockEntity;
    public final @Nullable Consumer<Matrix4f> transform;

    public RawBlockWidget(BlockState blockState, @Nullable BlockEntity blockEntity, @Nullable Consumer<Matrix4f> transform) {
        this.blockState = blockState;
        this.blockEntity = blockEntity;
        this.transform = transform;
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    // ---

    public static class Instance extends LeafWidgetInstance<RawBlockWidget> {

        public static final Size DEFAULT_SIZE = Size.square(16);

        public Instance(RawBlockWidget widget) {
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
        public void draw(BraidDrawContext ctx) {
            var client = this.host().client();

            ctx.push();
            ctx.translate(this.transform.width() / 2, this.transform.height() / 2, 100);
            ctx.scale(40 * (float) (this.transform.width() / 64f), -40 * (float) (this.transform.height() / 64f), 40);

            if (this.widget.transform != null) {
                this.widget.transform.accept(ctx.getMatrices().peek().getPositionMatrix());
            } else {
                ctx.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
                ctx.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45 + 180));
            }

            ctx.translate(-.5f, -.5f, -.5f);

            if (this.widget.blockState.getRenderType() != BlockRenderType.INVISIBLE) {
                client.getBlockRenderManager().renderBlockAsEntity(
                    this.widget.blockState,
                    ctx.getMatrices(),
                    ctx.getVertexConsumers(),
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
                        ctx.getMatrices(),
                        ctx.getVertexConsumers(),
                        LightmapTextureManager.MAX_LIGHT_COORDINATE,
                        OverlayTexture.DEFAULT_UV
                    );
                }
            }

            DiffuseLighting.disableGuiDepthLighting();
            ctx.getVertexConsumers().draw();
            DiffuseLighting.enableGuiDepthLighting();

            ctx.pop();
        }
    }
}
