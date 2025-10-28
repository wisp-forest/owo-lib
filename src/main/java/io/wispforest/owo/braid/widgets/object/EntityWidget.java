package io.wispforest.owo.braid.widgets.object;

import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.util.pond.OwoEntityRenderDispatcherExtension;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.function.Consumer;

public class EntityWidget extends LeafInstanceWidget {

    public final double scale;
    public final Entity entity;

    protected DisplayMode displayMode = DisplayMode.FIXED;
    protected boolean scaleToFit = true;
    protected boolean showNametag = false;
    protected @Nullable Consumer<Matrix4f> transform = null;

    public EntityWidget(double scale, Entity entity, @Nullable WidgetSetupCallback<EntityWidget> setupCallback) {
        this.scale = scale;
        this.entity = entity;
        if (setupCallback != null) setupCallback.setup(this);
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

    public EntityWidget transform(Consumer<Matrix4f> transform) {
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
                    this.transform.width() / this.widget.entity.getWidth(),
                    this.transform.height() / this.widget.entity.getHeight()
                ) * .6;
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

            var entitySpaceToWidgetSpace = new Matrix4f();
            entitySpaceToWidgetSpace.translate((float) (this.transform.width() / 2), (float) (this.transform.height() / 2), 100);
            entitySpaceToWidgetSpace.scale((float) (this.widget.scale * this.baseScale));
            entitySpaceToWidgetSpace.scale(1, -1, 1);

            var entityTransform = new Matrix4f();
            if (this.widget.transform != null) {
                this.widget.transform.accept(entityTransform);
            }

            entityTransform.translate(0, -entity.getHeight() / 2, 0);

            var xRotation = 0f;
            var yRotation = 0f;

            var prevHeadYaw = entity instanceof LivingEntity living ? living.prevHeadYaw : 0;
            var prevYaw = entity.prevYaw;
            var prevPitch = entity.prevPitch;

            if (this.widget.displayMode == DisplayMode.FIXED) {
                xRotation = 35;
                yRotation = -45;
            } else if (this.widget.displayMode != DisplayMode.NONE) {
                var globalCursorPos = this.host().cursorPosition();
                var cursorTransform = new Matrix4f(ctx.getMatrices().peek().getPositionMatrix())
                    .mul(entitySpaceToWidgetSpace)
                    .mul(entityTransform)
                    .invert();

                var localCursorPos = cursorTransform.transform(new Vector4f((float) globalCursorPos.x(), (float) globalCursorPos.y(), 0, 1));

                switch (widget.displayMode) {
                    case CURSOR -> {
                        var center = new Vector4f(0, entity.getEyeHeight(entity.getPose()), 0, 1);

                        xRotation = (float) Math.toDegrees(Math.atan(localCursorPos.y - center.y)) * -.15f;
                        yRotation = (float) Math.toDegrees(Math.atan(localCursorPos.x - center.x)) * .15f;
                        if (entity instanceof LivingEntity living) living.prevHeadYaw = -yRotation * 3;

                        entity.prevYaw = -yRotation * .65f;
                        entity.prevPitch = xRotation * 2.5f;
                    }
                    case VANILLA -> {
                        var center = new Vector4f(0, entity.getHeight() / 2, 0, 1);

                        xRotation = (float) Math.atan(localCursorPos.y - center.y) * -20f;
                        yRotation = (float) Math.atan(localCursorPos.x - center.x) * 20f;
                        if (entity instanceof LivingEntity living) living.prevHeadYaw = -yRotation;

                        entity.prevYaw = -yRotation;
                        entity.prevPitch = xRotation;
                    }
                }
            }

            // We make sure the yRotation never becomes 0, as the lighting otherwise becomes very unhappy
            if (yRotation == 0) yRotation = .1f;

            entityTransform.rotate(RotationAxis.POSITIVE_X.rotationDegrees(xRotation));
            entityTransform.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation));

            ctx.multiplyPositionMatrix(entitySpaceToWidgetSpace);
            ctx.multiplyPositionMatrix(entityTransform);

            var dispatcher = this.host().client().getEntityRenderDispatcher();
            var dispatcherExtension = (OwoEntityRenderDispatcherExtension) dispatcher;
            dispatcherExtension.owo$setShowNametag(this.widget.showNametag);

            // TODO: counterrotate properly
            dispatcher.setRotation(entityTransform.getUnnormalizedRotation(new Quaternionf()).normalize().invert());

            DiffuseLighting.enableForLevel();
            dispatcher.setRenderShadows(false);
            dispatcher.render(entity, 0, 0, 0, 0, 0, ctx.getMatrices(), ctx.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
            dispatcher.setRenderShadows(true);
            ctx.draw();
            DiffuseLighting.enableGuiDepthLighting();

            ctx.pop();

            dispatcherExtension.owo$setShowNametag(true);
            if (entity instanceof LivingEntity living) living.prevHeadYaw = prevHeadYaw;
            entity.prevPitch = prevPitch;
            entity.prevYaw = prevYaw;
        }
    }

    public enum DisplayMode {
        FIXED, VANILLA, CURSOR, NONE
    }
}
