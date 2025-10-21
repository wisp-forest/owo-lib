package io.wispforest.owo.ui.component;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ui.access.EntityRendererAccessor;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.renderstate.EntityElementRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.session.telemetry.TelemetrySender;
import net.minecraft.client.session.telemetry.WorldSession;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.world.ClientChunkLoadProgress;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.registry.Registries;
import net.minecraft.server.ServerLinks;
import net.minecraft.storage.NbtReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

public class EntityComponent<E extends Entity> extends BaseComponent {

    protected final EntityRenderManager manager;
    protected final VertexConsumerProvider.Immediate entityBuffers;
    protected final E entity;

    protected float mouseRotation = 0;
    protected float scale = 1;
    protected boolean lookAtCursor = false;
    protected boolean allowMouseRotation = false;
    protected boolean scaleToFit = false;
    protected boolean showNametag = false;
    protected Consumer<Matrix4f> transform = matrixStack -> {};

    protected EntityComponent(Sizing sizing, E entity) {
        final var client = MinecraftClient.getInstance();
        this.manager = client.getEntityRenderDispatcher();
        this.entityBuffers = client.getBufferBuilders().getEntityVertexConsumers();

        this.entity = entity;

        this.sizing(sizing);
    }

    @SuppressWarnings("DataFlowIssue")
    protected EntityComponent(Sizing sizing, EntityType<E> type, @Nullable NbtCompound nbt) {
        final var client = MinecraftClient.getInstance();
        this.manager = client.getEntityRenderDispatcher();
        this.entityBuffers = client.getBufferBuilders().getEntityVertexConsumers();

        this.entity = type.create(client.world, SpawnReason.BREEDING);
        if (nbt != null) entity.readData(NbtReadView.create(new ErrorReporter.Logging(Owo.LOGGER), client.world.getRegistryManager(), nbt));
        entity.updatePosition(client.player.getX(), client.player.getY(), client.player.getZ());

        this.sizing(sizing);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        var matrix = new Matrix4f();
        matrix.scale(75 * this.scale * this.width / 64f, -75 * this.scale * this.height / 64f, -75 * this.scale);

        matrix.translate(0, entity.getHeight() / 2f, 0);

        this.transform.accept(matrix);

        if (this.lookAtCursor) {
            float xRotation = (float) Math.toDegrees(Math.atan((mouseY - this.y - this.height / 2f) / 40f));
            float yRotation = (float) Math.toDegrees(Math.atan((mouseX - this.x - this.width / 2f) / 40f));

            if (this.entity instanceof LivingEntity living) {
                living.lastHeadYaw = -yRotation;
            }

            this.entity.lastYaw = -yRotation;
            this.entity.lastPitch = xRotation * .65f;

            // We make sure the xRotation never becomes 0, as the lighting otherwise becomes very unhappy
            if (xRotation == 0) xRotation = .1f;
            matrix.rotate(RotationAxis.POSITIVE_X.rotationDegrees(xRotation * .15f));
            matrix.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation * .15f));
        } else {
            matrix.rotate(RotationAxis.POSITIVE_X.rotationDegrees(35));
            matrix.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(-45 + this.mouseRotation));
        }

        var entityState = this.manager.getAndUpdateRenderState(this.entity, partialTicks);
        var renderer = this.manager.getRenderer(this.entity);

        if (showNametag) {
            entityState.displayName = ((EntityRendererAccessor) renderer).owo$getDisplayName(entity);
            entityState.nameLabelPos = entity.getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, entity.getLerpedYaw(partialTicks));
        } else {
            entityState.displayName = null;
            entityState.nameLabelPos = null;
        }

        context.state.addSpecialElement(new EntityElementRenderState(
            entityState,
            matrix,
            new ScreenRect(this.x, this.y, this.width, this.height),
            context.scissorStack.peekLast()
        ));
    }

    @Override
    public boolean onMouseDrag(Click click, double deltaX, double deltaY) {
        if (this.allowMouseRotation && click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.mouseRotation += deltaX;

            super.onMouseDrag(click, deltaX, deltaY);
            return true;
        } else {
            return super.onMouseDrag(click, deltaX, deltaY);
        }
    }

    public E entity() {
        return this.entity;
    }

    public EntityComponent<E> allowMouseRotation(boolean allowMouseRotation) {
        this.allowMouseRotation = allowMouseRotation;
        return this;
    }

    public boolean allowMouseRotation() {
        return this.allowMouseRotation;
    }

    public EntityComponent<E> lookAtCursor(boolean lookAtCursor) {
        this.lookAtCursor = lookAtCursor;
        return this;
    }

    public boolean lookAtCursor() {
        return this.lookAtCursor;
    }

    public EntityComponent<E> scale(float scale) {
        this.scale = scale;
        return this;
    }

    public float scale() {
        return this.scale;
    }

    public EntityComponent<E> scaleToFit(boolean scaleToFit) {
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

    public EntityComponent<E> transform(Consumer<Matrix4f> transform) {
        this.transform = transform;
        return this;
    }

    public Consumer<Matrix4f> transform() {
        return transform;
    }

    public EntityComponent<E> showNametag(boolean showNametag) {
        this.showNametag = showNametag;
        return this;
    }

    public boolean showNametag() {
        return showNametag;
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.MOUSE_CLICK;
    }

    public static RenderablePlayerEntity createRenderablePlayer(GameProfile profile) {
        return new RenderablePlayerEntity(profile);
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(children, "scale", UIParsing::parseFloat, this::scale);
        UIParsing.apply(children, "look-at-cursor", UIParsing::parseBool, this::lookAtCursor);
        UIParsing.apply(children, "mouse-rotation", UIParsing::parseBool, this::allowMouseRotation);
        UIParsing.apply(children, "scale-to-fit", UIParsing::parseBool, this::scaleToFit);
    }

    public static EntityComponent<?> parse(Element element) {
        UIParsing.expectAttributes(element, "type");
        var entityId = UIParsing.parseIdentifier(element.getAttributeNode("type"));
        var entityType = Registries.ENTITY_TYPE.getOptionalValue(entityId).orElseThrow(() -> new UIModelParsingException("Unknown entity type " + entityId));

        NbtCompound nbt = null;
        if (element.hasAttribute("nbt")) {
            try {
                nbt = StringNbtReader.readCompound(element.getAttribute("nbt"));
            } catch (CommandSyntaxException cse) {
                throw new UIModelParsingException("Invalid NBT compound", cse);
            }
        }

        return new EntityComponent<>(Sizing.content(), entityType, nbt);
    }

    public static class RenderablePlayerEntity extends ClientPlayerEntity {

        protected SkinTextures skinTextures;

        protected RenderablePlayerEntity(GameProfile profile) {
            super(MinecraftClient.getInstance(),
                MinecraftClient.getInstance().world,
                new ClientPlayNetworkHandler(MinecraftClient.getInstance(),
                    new ClientConnection(NetworkSide.CLIENTBOUND),
                    new ClientConnectionState(
                        new ClientChunkLoadProgress(0),
                        profile, new WorldSession(TelemetrySender.NOOP, false, Duration.ZERO, ""),
                        MinecraftClient.getInstance().world.getRegistryManager().toImmutable(),
                        MinecraftClient.getInstance().world.getEnabledFeatures(),
                        "Wisp Forest Enterprises", null, null, Map.of(), null, Map.of(), ServerLinks.EMPTY, Map.of(),
                        true
                    )),
                null, null, PlayerInput.DEFAULT, false
            );

            this.skinTextures = DefaultSkinHelper.getSkinTextures(profile);
            Util.getMainWorkerExecutor().execute(() -> {
                var completeProfile = MinecraftClient.getInstance().getApiServices().profileResolver().getProfileById(profile.id()).orElse(profile);

                this.skinTextures = DefaultSkinHelper.getSkinTextures(completeProfile);
                this.client.getSkinProvider().fetchSkinTextures(completeProfile).thenAccept(textures -> {
                    textures.ifPresent($ -> this.skinTextures = $);
                });
            });
        }

        @Override
        public SkinTextures getSkin() {
            return this.skinTextures;
        }

        @Override
        public boolean isModelPartVisible(PlayerModelPart part) {
            return true;
        }

        @Nullable
        @Override
        protected PlayerListEntry getPlayerListEntry() {
            return null;
        }
    }
}
