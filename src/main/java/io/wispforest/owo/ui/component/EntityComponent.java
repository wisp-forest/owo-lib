package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.Map;

public class EntityComponent extends BaseComponent {

    protected final EntityRenderDispatcher dispatcher;
    protected final VertexConsumerProvider.Immediate entityBuffers;
    protected final Entity entity;

    protected float mouseRotation = 0;
    protected float scale = 1;
    protected boolean lookAtCursor = false;
    protected boolean allowMouseRotation = false;
    protected boolean scaleToFit = false;

    protected EntityComponent(Entity entity) {
        final var client = MinecraftClient.getInstance();
        this.dispatcher = client.getEntityRenderDispatcher();
        this.entityBuffers = client.getBufferBuilders().getEntityVertexConsumers();

        this.entity = entity;
    }

    protected EntityComponent(EntityType<?> type, @Nullable NbtCompound nbt) {
        final var client = MinecraftClient.getInstance();
        this.dispatcher = client.getEntityRenderDispatcher();
        this.entityBuffers = client.getBufferBuilders().getEntityVertexConsumers();

        this.entity = type.create(client.world);
        if (nbt != null) entity.readNbt(nbt);
        entity.updatePosition(client.player.getX(), client.player.getY(), client.player.getZ());
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        matrices.push();

        matrices.translate(x + this.width / 2f, y + this.height / 2f, 100);
        matrices.scale(75 * this.scale * this.width / 64f, -75 * this.scale * this.height / 64f, 75 * this.scale);

        matrices.translate(0, entity.getHeight() / -2f, 0);

        if (this.lookAtCursor) {
            float xRotation = (float) Math.toDegrees(Math.atan((mouseY - this.y - this.height / 2f) / 40f));
            float yRotation = (float) Math.toDegrees(Math.atan((mouseX - this.x - this.width / 2f) / 40f));

            if (this.entity instanceof LivingEntity living) {
                living.prevHeadYaw = -yRotation;
            }

            this.entity.prevYaw = -yRotation;
            this.entity.prevPitch = xRotation * .65f;

            // We make sure the xRotation never becomes 0, as the lighting otherwise becomes very unhappy
            if (xRotation == 0) xRotation = .1f;
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(xRotation * .15f));
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(yRotation * .15f));
        } else {
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(35));
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-45 + this.mouseRotation));
        }

        RenderSystem.setShaderLights(new Vec3f(.15f, 1, 0), new Vec3f(.15f, -1, 0));
        this.dispatcher.setRenderShadows(false);
        this.dispatcher.render(this.entity, 0, 0, 0, 0, 0, matrices, this.entityBuffers, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        this.dispatcher.setRenderShadows(true);
        this.entityBuffers.draw();
        DiffuseLighting.enableGuiDepthLighting();

        matrices.pop();
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        if (this.allowMouseRotation && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.mouseRotation += deltaX;
            return true;
        } else {
            return false;
        }
    }

    public Entity entity() {
        return this.entity;
    }

    public EntityComponent allowMouseRotation(boolean allowMouseRotation) {
        this.allowMouseRotation = allowMouseRotation;
        return this;
    }

    public boolean allowMouseRotation() {
        return this.allowMouseRotation;
    }

    public EntityComponent lookAtCursor(boolean lookAtCursor) {
        this.lookAtCursor = lookAtCursor;
        return this;
    }

    public boolean lookAtCursor() {
        return this.lookAtCursor;
    }

    public EntityComponent scale(float scale) {
        this.scale = scale;
        return this;
    }

    public float scale() {
        return this.scale;
    }

    public EntityComponent scaleToFit(boolean scaleToFit) {
        this.scaleToFit = scaleToFit;

        if (scaleToFit) {
            float xScale = .5f / entity.getWidth();
            float yScale = .5f / entity.getHeight();

            this.scale(Math.min(xScale, yScale));
        }

        return this;
    }

    public boolean scaleToFit() {
        return this.scaleToFit;
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.MOUSE_CLICK;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(children, "scale", UIParsing::parseFloat, this::scale);
        UIParsing.apply(children, "look-at-cursor", UIParsing::parseBool, this::lookAtCursor);
        UIParsing.apply(children, "mouse-rotation", UIParsing::parseBool, this::allowMouseRotation);
        UIParsing.apply(children, "scale-to-fit", UIParsing::parseBool, this::scaleToFit);
    }

    public static EntityComponent parse(Element element) {
        UIParsing.expectAttributes(element, "type");
        var entityId = UIParsing.parseIdentifier(element.getAttributeNode("type"));
        var entityType = Registry.ENTITY_TYPE.getOrEmpty(entityId).orElseThrow(() -> new UIModelParsingException("Unknown entity type " + entityId));

        return new EntityComponent(entityType, null);
    }
}
