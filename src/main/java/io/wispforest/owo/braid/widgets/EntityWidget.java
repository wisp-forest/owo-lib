package io.wispforest.owo.braid.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.util.pond.OwoEntityRenderDispatcherExtension;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;
import org.apache.commons.lang3.builder.Diff;
import org.joml.Vector3f;

public class EntityWidget extends LeafInstanceWidget {

    public final double scale;
    public final boolean lookAtCursor;
    public final boolean scaleToFit;
    public final boolean showNametag;
    public final Entity entity;

    public EntityWidget(double scale, boolean lookAtCursor, boolean scaleToFit, boolean showNametag, Entity entity) {
        this.scale = scale;
        this.lookAtCursor = lookAtCursor;
        this.scaleToFit = scaleToFit;
        this.showNametag = showNametag;
        this.entity = entity;
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
        public void draw(OwoUIDrawContext ctx) {
            ctx.push();

            var scale = this.widget.scale * this.baseScale;

            ctx.translate(this.transform.width() / 2f, this.transform.height() / 2f, 100);
            ctx.scale(
                (float) (75 * scale * this.transform.width() / 64f),
                (float) (-75 * scale * this.transform.height() / 64f),
                (float) (75 * scale)
            );

            ctx.translate(0, this.widget.entity.getHeight() / -2f, 0);

//            this.transform.accept(matrices);

            if (this.widget.lookAtCursor) {
//                float xRotation = (float) Math.toDegrees(Math.atan((mouseY - this.y - this.height / 2f) / 40f));
//                float yRotation = (float) Math.toDegrees(Math.atan((mouseX - this.x - this.width / 2f) / 40f));
//
//                if (this.entity instanceof LivingEntity living) {
//                    living.prevHeadYaw = -yRotation;
//                }
//
//                this.entity.prevYaw = -yRotation;
//                this.entity.prevPitch = xRotation * .65f;
//
//                // We make sure the xRotation never becomes 0, as the lighting otherwise becomes very unhappy
//                if (xRotation == 0) xRotation = .1f;
//                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xRotation * .15f));
//                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation * .15f));
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
