package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.instance.MouseListener;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.util.pond.OwoEntityRenderDispatcherExtension;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.function.Consumer;

public class EntityWidget extends LeafInstanceWidget {

    public final double scale;
    public final Entity entity;

    protected boolean lookAtCursor = false;
    protected boolean scaleToFit = true;
    protected boolean showNametag = false;
    protected @Nullable Consumer<MatrixStack> transform = null;

    public EntityWidget(double scale, Entity entity, @Nullable WidgetSetupCallback<EntityWidget> setupCallback) {
        this.scale = scale;
        this.entity = entity;
        if (setupCallback != null) setupCallback.setup(this);
    }

    //region Setup Methods

    public EntityWidget lookAtCursor(boolean lookAtCursor) {
        this.lookAtCursor = lookAtCursor;
        return this;
    }

    public boolean lookAtCursor() {
        return this.lookAtCursor;
    }

    public EntityWidget scaleToFit(boolean scaleToFit) {
        this.scaleToFit = scaleToFit;
        return this;
    }

    public boolean scaleToFit() {
        return this.scaleToFit;
    }

    public EntityWidget showNametag(boolean showNametag) {
        this.showNametag = showNametag;
        return this;
    }

    public boolean showNametag() {
        return this.showNametag;
    }

    public EntityWidget transform(Consumer<MatrixStack> transform) {
        this.transform = transform;
        return this;
    }

    public @Nullable Consumer<MatrixStack> transform() {
        return this.transform;
    }

    //endregion

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<EntityWidget> {

        protected double baseScale = 1.0;

        public Instance(EntityWidget widget) {
            super(widget);
        }

        @Override
        public void setWidget(EntityWidget widget) {
            if (this.widget.scaleToFit != widget.scaleToFit) {
                this.markNeedsLayout();
            }

            super.setWidget(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.transform.setSize(constraints.minSize());

            if (this.widget.scaleToFit) {
                this.baseScale = Math.min(
                    .5f / this.widget.entity.getWidth(),
                    .5f / this.widget.entity.getHeight()
                );
            }
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return 32;
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return 32;
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.empty();
        }

        @Override
        public void draw(BraidDrawContext ctx) {
            ctx.push();

            var scale = this.widget.scale * this.baseScale;

            ctx.translate(this.transform.width() / 2f, this.transform.height() / 2f, 100);
            ctx.scale(
                (float) (75 * scale * this.transform.width() / 64f),
                (float) (-75 * scale * this.transform.height() / 64f),
                (float) (75 * scale)
            );

            ctx.translate(0, this.widget.entity.getHeight() / -2f, 0);

            if (this.widget.transform != null) this.widget.transform.accept(ctx.getMatrices());

            if (this.widget.lookAtCursor) {
                //TODO: apply the custom transform here so looking at cursor still works
                var cursorPos = this.host().cursorPosition();

                var globalTransform = this.computeGlobalTransform();
                var localCursor = new Vector4f((float) cursorPos.x(), (float) cursorPos.y(), 0, 1);
                globalTransform.transform(localCursor);

                float centerX = (float) (this.transform.width() / 2f);
                float centerY = (float) (this.transform.height() / 2f);

                float entityBottomY = centerY + (float) (this.widget.entity.getHeight() * this.baseScale * this.widget.scale * 75 * this.transform.height() / 64f / 2f);

                float eyeY = entityBottomY - (float) (this.widget.entity.getEyeHeight(this.widget.entity.getPose()) * this.baseScale * this.widget.scale * 75 * this.transform.height() / 64f);

                float deltaX = localCursor.x - centerX;
                float deltaY = localCursor.y - eyeY;

                float xRotation = (float) Math.toDegrees(Math.atan(deltaY / 40f));
                float yRotation = (float) Math.toDegrees(Math.atan(deltaX / 40f));

                if (widget.entity instanceof LivingEntity living) {
                    living.prevHeadYaw = -yRotation;
                }

                widget.entity.prevYaw = -yRotation;
                widget.entity.prevPitch = xRotation * .65f;

                // We make sure the xRotation never becomes 0, as the lighting otherwise becomes very unhappy
                if (xRotation == 0) xRotation = .1f;
                ctx.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xRotation * .15f));
                ctx.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation * .15f));
            } else {
                ctx.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
                ctx.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45/* + this.mouseRotation*/));
            }

            var dispatcher = this.host().client().getEntityRenderDispatcher();
            var dispatcherExtension = (OwoEntityRenderDispatcherExtension) dispatcher;
            dispatcherExtension.owo$setShowNametag(this.widget.showNametag);

            dispatcher.setRotation(RotationAxis.POSITIVE_Y.rotationDegrees(45));

            DiffuseLighting.enableForLevel();
            dispatcher.setRenderShadows(false);
            dispatcher.render(this.widget.entity, 0, 0, 0, 0, ctx.getMatrices(), ctx.vertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
            dispatcher.setRenderShadows(true);
            ctx.draw();
            DiffuseLighting.enableGuiDepthLighting();

            ctx.pop();

            dispatcherExtension.owo$setShowNametag(true);
        }
    }
}
