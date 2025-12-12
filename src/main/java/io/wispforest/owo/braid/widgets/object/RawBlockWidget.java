package io.wispforest.owo.braid.widgets.object;

import com.mojang.math.Axis;
import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.core.element.BraidBlockElement;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
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
        public void draw(BraidGraphics graphics) {
            var drawTransform = new Matrix4f();
            drawTransform.scale(40 * (float) (this.transform.width() / 64f), -40 * (float) (this.transform.height() / 64f), -40);

            if (this.widget.transform != null) {
                this.widget.transform.accept(drawTransform);
            } else {
                drawTransform.rotate(Axis.XP.rotationDegrees(30));
                drawTransform.rotate(Axis.YP.rotationDegrees(45 + 180));
            }

            drawTransform.translate(-.5f, -.5f, -.5f);

            BlockEntityRenderState entity = null;
            if (this.widget.blockEntity != null) {
                var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(this.widget.blockEntity);
                if (renderer != null) {
                    entity = renderer.createRenderState();
                    renderer.extractRenderState(
                        this.widget.blockEntity, entity, 0, Vec3.ZERO, null
                    );
                }
            }

            graphics.guiRenderState.submitPicturesInPictureState(new BraidBlockElement(
                this.widget.blockState,
                entity,
                drawTransform,
                new Matrix3x2f(graphics.pose()),
                this.transform.width(),
                this.transform.height(),
                graphics.scissorStack.peek()
            ));
        }
    }
}
