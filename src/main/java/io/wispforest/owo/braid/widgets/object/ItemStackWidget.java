package io.wispforest.owo.braid.widgets.object;

import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

import java.util.OptionalDouble;
import java.util.function.Consumer;

/// A widget that renders an [ItemStack]
///
/// The stack is rendered using the specified [ModelTransformationMode]
/// and can show overlay information (item bar, count, cooldown progress, etc.)
public class ItemStackWidget extends LeafInstanceWidget {

    public final ItemStack stack;
    protected boolean showOverlay = true;
    protected ModelTransformationMode transformationMode = ModelTransformationMode.GUI;
    protected @Nullable LightOverride lightOverride = null;
    protected @Nullable Consumer<Matrix4f> transform;

    public ItemStackWidget(ItemStack stack, @Nullable WidgetSetupCallback<ItemStackWidget> setupCallback) {
        this.stack = stack;
        if (setupCallback != null) setupCallback.setup(this);
    }

    public ItemStackWidget(ItemStack stack) {
        this(stack, null);
    }

    public ItemStackWidget showOverlay(boolean showOverlay) {
        this.assertMutable();
        this.showOverlay = showOverlay;
        return this;
    }

    public boolean showOverlay() {
        return this.showOverlay;
    }

    public ItemStackWidget transformationMode(ModelTransformationMode transformationMode) {
        this.assertMutable();
        this.transformationMode = transformationMode;
        this.showOverlay = false;
        return this;
    }

    public ModelTransformationMode transformationMode() {
        return this.transformationMode;
    }

    public ItemStackWidget lightOverride(@Nullable LightOverride lightOverride) {
        this.assertMutable();
        this.lightOverride = lightOverride;
        return this;
    }

    public @Nullable LightOverride lightOverride() {
        return this.lightOverride;
    }

    public ItemStackWidget transform(@Nullable Consumer<Matrix4f> transform) {
        this.transform = transform;
        return this;
    }

    public @Nullable Consumer<Matrix4f> transform() {
        return this.transform;
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<ItemStackWidget> {

        private static final Matrix4f ITEM_SCALING = new Matrix4f().scaling(16, -16, 16);
        public static final Size DEFAULT_SIZE = Size.square(16);

        public Instance(ItemStackWidget widget) {
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

            var notSideLit = !client.getItemRenderer().getModel(this.widget.stack, null, null, 0).isSideLit();
            if (notSideLit) {
                ctx.draw();
                DiffuseLighting.disableGuiDepthLighting();
            }

            var matrices = ctx.getMatrices();
            matrices.push();

            // Scale according to component size and translate to the center
            matrices.scale((float) (this.transform.width() / 16), (float) (this.transform.height() / 16), 1);
            matrices.translate(8.0, 8.0, 8.0);

            // Vanilla scaling and y inversion
            if (notSideLit) {
                matrices.scale(16, -16, 16);
            } else {
                matrices.multiplyPositionMatrix(ITEM_SCALING);
            }

            if (this.widget.transform != null) this.widget.transform.accept(matrices.peek().getPositionMatrix());

            client.getItemRenderer().renderItem(this.widget.stack, this.widget.transformationMode, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, matrices, ctx.getVertexConsumers(), client.world, 0);
            ctx.draw();

            // Clean up
            matrices.pop();

            if (this.widget.showOverlay) {
                ctx.drawItemInSlot(client.textRenderer, this.widget.stack, 0, 0);
            }
            if (notSideLit) {
                DiffuseLighting.enableGuiDepthLighting();
            }
        }
    }

    public enum LightOverride {
        FRONT,
        SIDE
    }
}
