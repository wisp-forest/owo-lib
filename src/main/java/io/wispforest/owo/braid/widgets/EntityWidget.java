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

import java.awt.*;
import java.util.OptionalDouble;
import java.util.function.Consumer;

public class EntityWidget extends LeafInstanceWidget {

    public final double scale;
    public final Entity entity;

    protected DisplayMode displayMode = DisplayMode.FIXED;
    protected boolean scaleToFit = true;
    protected boolean showNametag = false;
    protected @Nullable Consumer<MatrixStack> transform = matrixStack -> matrixStack.translate(0, Math.sin(System.currentTimeMillis() / 1000d), 0);

    public EntityWidget(double scale, Entity entity, @Nullable WidgetSetupCallback<EntityWidget> setupCallback) {
        this.scale = scale;
        this.entity = entity;
        if (setupCallback != null) setupCallback.setup(this);
        this.displayMode = DisplayMode.VANILLA;
    }

    public EntityWidget displayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
        return this;
    }

    public DisplayMode displayMode() {
        return this.displayMode;
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
            var entity = this.widget.entity;
            ctx.push();

            var scale = this.widget.scale * this.baseScale;

            ctx.translate(this.transform.width() / 2f, this.transform.height() / 2f, 100);
            ctx.scale(
                (float) (75 * scale * this.transform.width() / 64f),
                (float) (-75 * scale * this.transform.height() / 64f),
                (float) (75 * scale)
            );

            ctx.translate(0, entity.getHeight() / -2f, 0);
            var globalTransform = this.computeGlobalTransform();

            if (this.widget.transform != null) this.widget.transform.accept(ctx.getMatrices());

            if (this.widget.displayMode == DisplayMode.FIXED) {
                ctx.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
                ctx.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45/* + this.mouseRotation*/));
            } else {
                //TODO: apply the custom transform here so looking at cursor still works
                var cursorPos = this.host().cursorPosition();

                var matrix = new MatrixStack();
                if (this.widget.transform != null) this.widget.transform.accept(matrix);
                var localTransform = matrix.peek().getPositionMatrix();

                //TODO: AAAAAAAAAAAAAAAAAAAAA why the fuck doesnt applying the global transform work
                var localCursor = new Vector4f((float) cursorPos.x(), (float) cursorPos.y(), 0, 1);
                globalTransform.transform(localCursor);
//                localTransform.invert().transform(localCursor);

                //TODO: DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE DIE
                var center = new Vector4f((float) this.transform.width() / 2f, (float) this.transform.height() / 2f, 0, 1);
                localTransform.transform(center);

                switch (this.widget.displayMode) {
                    case VANILLA -> {
                        float xRotation = (float) Math.atan((localCursor.x - center.x) / 40f);
                        float yRotation = (float) Math.atan((localCursor.y - center.y) / 40f);
                        if (entity instanceof LivingEntity living) living.prevHeadYaw = -xRotation * 20f;

                        entity.prevYaw = -xRotation * 20f;
                        entity.prevPitch = yRotation * 20f;

                        if (yRotation == 0) yRotation = .1f;
                        ctx.multiply(RotationAxis.POSITIVE_X.rotationDegrees(yRotation * 20f));
                        ctx.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(xRotation * 20f));
                    }
                    case CURSOR -> {
                        var entityBottomY = center.y + entity.getHeight() * this.baseScale * this.widget.scale * 75 * this.transform.height() / 64f / 2f;

                        var eyeY = entityBottomY - (entity.getEyeHeight(entity.getPose()) * this.baseScale * this.widget.scale * 75 * this.transform.height() / 64f);

                        var xRotation = (float) Math.toDegrees(Math.atan((localCursor.x - center.x) / 40f));
                        var yRotation = (float) Math.toDegrees(Math.atan((localCursor.y - eyeY) / 40f));
                        if (entity instanceof LivingEntity living) living.prevHeadYaw = -xRotation;

                        entity.prevYaw = -xRotation;
                        entity.prevPitch = yRotation * .65f;

                        // We make sure the yRotation never becomes 0, as the lighting otherwise becomes very unhappy
                        if (yRotation == 0) yRotation = .1f;
                        ctx.multiply(RotationAxis.POSITIVE_X.rotationDegrees(yRotation * .15f));
                        ctx.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(xRotation * .15f));
                    }
                }
            }

            var dispatcher = this.host().client().getEntityRenderDispatcher();
            var dispatcherExtension = (OwoEntityRenderDispatcherExtension) dispatcher;
            dispatcherExtension.owo$setShowNametag(this.widget.showNametag);

            //TODO: rotate this like the opposite of the entity rotation idk
            dispatcher.setRotation(RotationAxis.POSITIVE_Y.rotationDegrees(0));

            DiffuseLighting.enableForLevel();
            dispatcher.setRenderShadows(false);
            dispatcher.render(entity, 0, 0, 0, 0, ctx.getMatrices(), ctx.vertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
            dispatcher.setRenderShadows(true);
            ctx.draw();
            DiffuseLighting.enableGuiDepthLighting();

            ctx.pop();

            dispatcherExtension.owo$setShowNametag(true);
        }
    }

    public enum DisplayMode {
        FIXED, VANILLA, CURSOR
    }
}
