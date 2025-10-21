package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.core.element.BraidItemElement;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

import java.util.OptionalDouble;
import java.util.function.Consumer;

/// A widget that renders an [ItemStack]
///
/// The stack is rendered using the specified [ItemDisplayContext]
/// and can show overlay information (item bar, count, cooldown progress, etc.)
public class ItemStackWidget extends LeafInstanceWidget {

    public final ItemStack stack;
    protected boolean showOverlay = true;
    protected ItemDisplayContext displayContext = ItemDisplayContext.GUI;
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

    public ItemStackWidget displayContext(ItemDisplayContext displayContext) {
        this.assertMutable();
        this.displayContext = displayContext;
        this.showOverlay = false;
        return this;
    }

    public ItemDisplayContext displayContext() {
        return this.displayContext;
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

        public static final Size DEFAULT_SIZE = Size.square(16);
        protected static final ItemRenderState ITEM_RENDER_STATE = new ItemRenderState();

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
            if (this.transform.width() <= 16 && this.transform.height() <= 16 && this.widget.displayContext == ItemDisplayContext.GUI && this.widget.transform == null) {
                // scale according to widget size, since items assume a 16x16 window
                ctx.push().scale((float) (this.transform.width() / 16f), (float) (this.transform.height() / 16f));
                ctx.drawItem(this.widget.stack, 0, 0);
                ctx.pop();
            } else {
                var state = new ItemRenderState();
                this.host().client().getItemModelManager().update(state, this.widget.stack, this.widget.displayContext, this.host().client().world, this.host().client().player, 0);

                var transformThisFrame = new Matrix4f();
                if (this.widget.transform != null) {
                    this.widget.transform.accept(transformThisFrame);
                }

                ctx.state.addSpecialElement(new BraidItemElement(
                    state,
                    this.transform.width(),
                    this.transform.height(),
                    ctx.scissorStack.peekLast(),
                    transformThisFrame,
                    new Matrix3x2f(ctx.getMatrices())
                ));
            }

            if (this.widget.showOverlay) {
                ctx.drawStackOverlay(this.host().client().textRenderer, this.widget.stack, 0, 0);
            }
        }
    }

    public enum LightOverride {
        FRONT,
        SIDE
    }
}
