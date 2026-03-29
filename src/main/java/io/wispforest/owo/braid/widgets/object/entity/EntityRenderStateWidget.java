package io.wispforest.owo.braid.widgets.object.entity;

import com.google.common.base.Preconditions;
import com.mojang.math.Axis;
import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.element.BraidEntityElement;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntityRenderStateWidget extends LeafInstanceWidget {

    public final double scale;
    public final Supplier<EntityRenderState> renderStateSupplier;

    protected EntityDisplayMode displayMode = EntityDisplayMode.FIXED;
    protected boolean scaleToFit = true;
    protected boolean showNametag = false;
    protected @Nullable Consumer<Matrix4f> transform = null;

    public EntityRenderStateWidget(double scale, Supplier<EntityRenderState> renderStateSupplier, @Nullable WidgetSetupCallback<EntityRenderStateWidget> setupCallback) {
        this.scale = scale;
        Preconditions.checkNotNull(renderStateSupplier, "The RenderState provided to an EntityRenderStateWidget cannot be null");
        this.renderStateSupplier = renderStateSupplier;
        if (setupCallback != null) setupCallback.setup(this);
    }

    public EntityRenderStateWidget(double scale, EntityRenderState renderState, @Nullable WidgetSetupCallback<EntityRenderStateWidget> setupCallback) {
        this(scale, () -> renderState, setupCallback);
    }

    public EntityRenderStateWidget(double scale, Supplier<EntityRenderState> renderStateSupplier) {
        this(scale, renderStateSupplier, null);
    }

    public EntityRenderStateWidget(double scale, EntityRenderState renderState) {
        this(scale, renderState, null);
    }

    public EntityRenderStateWidget displayMode(EntityDisplayMode displayMode) {
        this.displayMode = displayMode;
        return this;
    }

    public EntityDisplayMode displayMode() {
        return this.displayMode;
    }

    public EntityRenderStateWidget scaleToFit(boolean scaleToFit) {
        this.scaleToFit = scaleToFit;
        return this;
    }

    public boolean scaleToFit() {
        return this.scaleToFit;
    }

    public EntityRenderStateWidget showNametag(boolean showNametag) {
        this.showNametag = showNametag;
        return this;
    }

    public boolean showNametag() {
        return this.showNametag;
    }

    public EntityRenderStateWidget transform(Consumer<Matrix4f> transform) {
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

    public static class Instance extends LeafWidgetInstance<EntityRenderStateWidget> {

        protected double baseScale = 1.0;

        public Instance(EntityRenderStateWidget widget) {
            super(widget);
        }

        @Override
        public void setWidget(EntityRenderStateWidget widget) {
            if (this.widget.scaleToFit != widget.scaleToFit) {
                this.markNeedsLayout();
            }

            super.setWidget(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.transform.setSize(constraints.minSize());

            var renderState = this.widget.renderStateSupplier.get();

            if (this.widget.scaleToFit) {
                this.baseScale = Math.min(
                    this.transform.width() / renderState.boundingBoxWidth,
                    this.transform.height() / renderState.boundingBoxHeight
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
        public void draw(BraidGraphics graphics) {
            var state = this.widget.renderStateSupplier.get();

            var entitySpaceToWidgetSpace = new Matrix4f();
            entitySpaceToWidgetSpace.translate(0, (float) (this.transform.height() / 2), 100);
            entitySpaceToWidgetSpace.scale((float) (this.widget.scale * this.baseScale));
            entitySpaceToWidgetSpace.scale(1, -1, -1);

            var entityTransform = new Matrix4f();
            if (this.widget.transform != null) {
                this.widget.transform.accept(entityTransform);
            }

            entityTransform.translate(0, -state.boundingBoxHeight / 2, 0);

            var xRotation = 0f;
            var yRotation = 0f;

//            var lastHeadYaw = renderState instanceof LivingEntityRenderState livingState ? livingState.yHeadRotO : 0;
//            var lastYaw = renderState.yRotO;
//            var lastPitch = renderState.xRotO;

            if (this.widget.displayMode == EntityDisplayMode.FIXED) {
                xRotation = 35;
                yRotation = -45;
            } else if (this.widget.displayMode != EntityDisplayMode.NONE) {
                var globalCursorPos = this.host().cursorPosition();
                var cursor4x4Buffer = graphics.pose().get4x4(new float[16]);

                var cursorTransform = new Matrix4f()
                    .set(cursor4x4Buffer)
                    // we do this ugly cursor-specific offset here to account for the
                    // centering being indiscriminately applied inside the PIP renderer
                    .translate((float) (this.transform.width() / 2), 0, 0)
                    .mul(entitySpaceToWidgetSpace)
                    .mul(entityTransform)
                    .invert();

                var localCursorPos = cursorTransform.transform(new Vector4f((float) globalCursorPos.x(), (float) globalCursorPos.y(), 0, 1));

                switch (widget.displayMode) {
                    case CURSOR -> {
                        var center = new Vector4f(0, state.eyeHeight, 0, 1);

                        xRotation = (float) Math.toDegrees(Math.atan(localCursorPos.y - center.y)) * -.15f;
                        yRotation = (float) Math.toDegrees(Math.atan(localCursorPos.x - center.x)) * .15f;
                        if (state instanceof LivingEntityRenderState living) {
                            living.yRot = -yRotation * 3;
                            //TODO: how does this not seem to matter for non living entities
                            living.bodyRot = -yRotation * .65f;
                            living.xRot = xRotation * 2.5f;
                        }
                    }
                    case VANILLA -> {
                        var center = new Vector4f(0, state.boundingBoxHeight / 2, 0, 1);

                        xRotation = (float) Math.atan(localCursorPos.y - center.y) * -20f;
                        yRotation = (float) Math.atan(localCursorPos.x - center.x) * 20f;
                        if (state instanceof LivingEntityRenderState living) {
                            living.yRot = -yRotation;
                            //TODO: how does this not seem to matter for non living entities
                            living.bodyRot = -yRotation;
                            living.xRot = xRotation;
                        }
                    }
                    case VANILLA_CURSOR -> {
                        //fuck you mojang
                        var center = new Vector4f(0, state.eyeHeight, 0, 1);

                        xRotation = (float) Math.atan(localCursorPos.y - center.y) * -20f;
                        yRotation = (float) Math.atan(localCursorPos.x - center.x) * 20f;
                        if (state instanceof LivingEntityRenderState living) {
                            living.yRot = -yRotation;
                            //TODO: how does this not seem to matter for non living entities
                            living.bodyRot = -yRotation;
                            living.xRot = xRotation;
                        }
                    }
                }
            }

            // We make sure the yRotation never becomes 0, as the lighting otherwise becomes very unhappy
            if (yRotation == 0) yRotation = .1f;

            entityTransform.rotate(Axis.XP.rotationDegrees(xRotation));
            entityTransform.rotate(Axis.YP.rotationDegrees(yRotation));

            if (!this.widget.showNametag) {
                state.nameTag = null;
            }

            graphics.guiRenderState.addPicturesInPictureState(new BraidEntityElement(
                state,
                new Matrix4f().mul(entitySpaceToWidgetSpace).mul(entityTransform),
                new Matrix3x2f(graphics.pose()),
                this.transform.width(), this.transform.height(),
                graphics.scissorStack.peek()
            ));

//            if (state instanceof LivingEntity living) living.yHeadRotO = lastHeadYaw;
//            state.xRotO = lastPitch;
//            state.yRotO = lastYaw;
        }
    }
}
