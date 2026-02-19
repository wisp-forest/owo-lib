package io.wispforest.owo.ui.component;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ui.access.EntityRendererAccessor;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.renderstate.EntityElementRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.ServerLinks;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.storage.TagValueInput;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

public class EntityComponent<E extends Entity> extends BaseUIComponent {

    protected final EntityRenderDispatcher manager;
    protected final MultiBufferSource.BufferSource entityBuffers;
    protected final E entity;

    protected float mouseRotation = 0;
    protected float scale = 1;
    protected boolean lookAtCursor = false;
    protected boolean allowMouseRotation = false;
    protected boolean scaleToFit = false;
    protected boolean showNametag = false;
    protected Consumer<Matrix4f> transform = matrixStack -> {};

    protected EntityComponent(Sizing sizing, E entity) {
        final var client = Minecraft.getInstance();
        this.manager = client.getEntityRenderDispatcher();
        this.entityBuffers = client.renderBuffers().bufferSource();

        this.entity = entity;

        this.sizing(sizing);
    }

    @SuppressWarnings("DataFlowIssue")
    protected EntityComponent(Sizing sizing, EntityType<E> type, @Nullable CompoundTag nbt) {
        final var client = Minecraft.getInstance();
        this.manager = client.getEntityRenderDispatcher();
        this.entityBuffers = client.renderBuffers().bufferSource();

        this.entity = type.create(client.level, EntitySpawnReason.BREEDING);
        if (nbt != null) entity.load(TagValueInput.create(new ProblemReporter.ScopedCollector(Owo.LOGGER), client.level.registryAccess(), nbt));
        entity.absSnapTo(client.player.getX(), client.player.getY(), client.player.getZ());

        this.sizing(sizing);
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        var matrix = new Matrix4f();
        matrix.scale(75 * this.scale * this.width / 64f, -75 * this.scale * this.height / 64f, -75 * this.scale);

        matrix.translate(0, entity.getBbHeight() / 2f, 0);

        this.transform.accept(matrix);

        if (this.lookAtCursor) {
            float xRotation = (float) Math.toDegrees(Math.atan((mouseY - this.y - this.height / 2f) / 40f));
            float yRotation = (float) Math.toDegrees(Math.atan((mouseX - this.x - this.width / 2f) / 40f));

            if (this.entity instanceof LivingEntity living) {
                living.yHeadRotO = -yRotation;
            }

            this.entity.yRotO = -yRotation;
            this.entity.xRotO = xRotation * .65f;

            // We make sure the xRotation never becomes 0, as the lighting otherwise becomes very unhappy
            if (xRotation == 0) xRotation = .1f;
            matrix.rotate(Axis.XP.rotationDegrees(xRotation * .15f));
            matrix.rotate(Axis.YP.rotationDegrees(yRotation * .15f));
        } else {
            matrix.rotate(Axis.XP.rotationDegrees(35));
            matrix.rotate(Axis.YP.rotationDegrees(-45 + this.mouseRotation));
        }

        var entityState = this.manager.extractEntity(this.entity, partialTicks);
        var renderer = this.manager.getRenderer(this.entity);

        if (showNametag) {
            entityState.nameTag = ((EntityRendererAccessor) renderer).owo$getNameTag(entity);
            entityState.nameTagAttachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot(partialTicks));
        } else {
            entityState.nameTag = null;
            entityState.nameTagAttachment = null;
        }

        graphics.guiRenderState.submitPicturesInPictureState(new EntityElementRenderState(
            entityState,
            matrix,
            new ScreenRectangle(this.x, this.y, this.width, this.height),
            graphics.scissorStack.peek()
        ));
    }

    @Override
    public boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
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
            float xScale = .5f / entity.getBbWidth();
            float yScale = .5f / entity.getBbHeight();

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
        var entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).orElseThrow(() -> new UIModelParsingException("Unknown entity type " + entityId));

        CompoundTag nbt = null;
        if (element.hasAttribute("nbt")) {
            try {
                nbt = TagParser.parseCompoundFully(element.getAttribute("nbt"));
            } catch (CommandSyntaxException cse) {
                throw new UIModelParsingException("Invalid NBT compound", cse);
            }
        }

        return new EntityComponent<>(Sizing.content(), entityType, nbt);
    }

    public static class RenderablePlayerEntity extends LocalPlayer {

        protected PlayerSkin skinTextures;

        protected RenderablePlayerEntity(GameProfile profile) {
            super(Minecraft.getInstance(),
                Minecraft.getInstance().level,
                new ClientPacketListener(Minecraft.getInstance(),
                    new net.minecraft.network.Connection(PacketFlow.CLIENTBOUND),
                    new CommonListenerCookie(
                        new LevelLoadTracker(0),
                        profile, new WorldSessionTelemetryManager(TelemetryEventSender.DISABLED, false, Duration.ZERO, ""),
                        Minecraft.getInstance().level.registryAccess().freeze(),
                        Minecraft.getInstance().level.enabledFeatures(),
                        "Wisp Forest Enterprises", null, null, Map.of(), null, Map.of(), ServerLinks.EMPTY, Map.of(),
                        true
                    )),
                null, null, Input.EMPTY, false
            );

            this.skinTextures = DefaultPlayerSkin.get(profile);
            Util.backgroundExecutor().execute(() -> {
                var completeProfile = Minecraft.getInstance().services().profileResolver().fetchById(profile.id()).orElse(profile);

                this.skinTextures = DefaultPlayerSkin.get(completeProfile);
                this.minecraft.getSkinManager().get(completeProfile).thenAccept(textures -> {
                    textures.ifPresent($ -> this.skinTextures = $);
                });
            });
        }

        @Override
        public PlayerSkin getSkin() {
            return this.skinTextures;
        }

        @Override
        public boolean isModelPartShown(PlayerModelPart part) {
            return true;
        }

        @Nullable
        @Override
        protected PlayerInfo getPlayerInfo() {
            return null;
        }
    }
}
