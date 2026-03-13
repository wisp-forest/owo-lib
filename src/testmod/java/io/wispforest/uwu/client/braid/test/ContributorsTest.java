package io.wispforest.uwu.client.braid.test;

import com.mojang.authlib.GameProfile;
import com.mojang.math.Axis;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.Button;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.intents.*;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.object.ItemStackWidget;
import io.wispforest.owo.braid.widgets.object.entity.EntityDisplayMode;
import io.wispforest.owo.braid.widgets.object.entity.EntityWidget;
import io.wispforest.owo.braid.widgets.sharedstate.ShareableState;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.uwu.client.braid.TestSelector;
import io.wispforest.uwu.items.UwuItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.CommonColors;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;

import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class ContributorsTest extends StatefulWidget {
    @Override
    public WidgetState<ContributorsTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<ContributorsTest> {

        private List<State.Contributor> contributors = this.genContributors();

        private List<State.Contributor> genContributors() {
            return List.of(
                new State.Contributor(UUID.fromString("b6c2d403-bf7c-4e19-b7a2-f64c9e44e56a"), "glisco", Component.translatable("text.uwu.glisco")),
                new State.Contributor(UUID.fromString("09de8a6d-86bf-4c15-bb93-ce3384ce4e96"), "chyzman", Component.translatable("text.uwu.chyz")),
                new State.Contributor(UUID.fromString("517253c6-5ae6-4a70-8e8f-b8515321f774"), "Dragon_Seeker", TextOps.withColor("blodhgarm", 0xae0000)),
                new State.Contributor(UUID.fromString("63db48b4-723a-4323-8d67-45679507fd82"), "GreatGrayOwl", TextOps.withColor("skibediah fœtus", 0x9b57d0)),
                new State.Contributor(UUID.fromString("91a033f7-1dd3-4858-9c7b-8fb61ba6363d"), "Noaaan", Component.literal("no" + "a".repeat((int) (1 + Math.random() * 7)) + "n"))
            );
        }

        @Override
        public Widget build(BuildContext notContext) {
            return new SharedState<>(
                State.MurderState::new,
                new Builder(context -> {
                    var murders = SharedState.get(context, State.MurderState.class).murders;
                    var eepies = SharedState.get(context, State.MurderState.class).eepies;
                    var bed = SharedState.get(context, State.MurderState.class).bed;
                    return new Column(
                        MainAxisAlignment.CENTER,
                        CrossAxisAlignment.CENTER,
                        new Padding(Insets.all(10)),
                        new Label(murders.compareTo(BigInteger.ZERO) > 0 ? Component
                            .literal("You have committed " + murders + " act" + (murders.compareTo(BigInteger.ONE) > 0 ? "s" : "") + " of " + Component
                                .translatableEscape("uwu.homicide")
                                .getString() + " against the owo contributors!" + (murders.compareTo(BigInteger.valueOf(1000)) > 0 ? "... wtf bro" : ""))
                            .withColor(CommonColors.RED) : Component.literal("OWO Contributors")),
                        new Label(eepies.compareTo(BigInteger.ZERO) > 0
                            ? Component
                            .literal("You have committed " + eepies + " act" + (eepies.compareTo(BigInteger.ONE) > 0 ? "s" : "") + " of " + Component
                                .translatableEscape("uwu.eepy")
                                .getString() + " against the owo contributors!" + (murders.compareTo(BigInteger.valueOf(1000)) > 0 ? "... idk" : ""))
                            .withColor(((BlockItem) bed.getItem()).getBlock().defaultBlockState().getMapColor(Minecraft.getInstance().level, BlockPos.ZERO).col)
                            : Component.empty()),
                        new Grid(
                            LayoutAxis.VERTICAL,
                            3,
                            Grid.CellFit.loose(),
                            Stream.concat(
                                    this.contributors.stream()
                                        .map(contributor -> {
                                            return new Padding(
                                                Insets.all(8),
                                                new Panel(
                                                    Panel.VANILLA_LIGHT,
                                                    new Padding(
                                                        Insets.all(8),
                                                        new Column(
                                                            MainAxisAlignment.CENTER,
                                                            CrossAxisAlignment.CENTER,
                                                            new Padding(Insets.top(4)),
                                                            List.of(
                                                                new State.FirePlayer(new GameProfile(contributor.uuid, contributor.name)),
                                                                new Label(
                                                                    LabelStyle.SHADOW,
                                                                    true,
                                                                    contributor
                                                                        .displayName()
                                                                        .copy()
                                                                        .setStyle(contributor.displayName
                                                                            .copy()
                                                                            .getStyle()
                                                                            .withHoverEvent(new HoverEvent.ShowEntity(new HoverEvent.EntityTooltipInfo(
                                                                                EntityType.PLAYER,
                                                                                contributor.uuid,
                                                                                contributor.displayName
                                                                            ))))
                                                                ),
                                                                new State.RatingBar()
                                                            )
                                                        )
                                                    )
                                                )
                                            );
                                        }),
                                    Stream.of(
                                        new Sized(
                                            20,
                                            20,
                                            new Button(
                                                () -> setState(() -> this.contributors = this.genContributors()),
                                                new Label(LabelStyle.SHADOW, true, Component.literal("☠"))
                                            )
                                        )
                                    )
                                )
                                .toList()
                        )
                    );
                })
            );
        }

        public static class MurderState extends ShareableState {
            public BigInteger murders = BigInteger.ZERO;
            public BigInteger eepies = BigInteger.ZERO;
            private ItemStack bed = UwuItems.BRAID.getDefaultInstance();
        }

        public record Contributor(UUID uuid, String name, Component displayName) {}

        public static class FirePlayer extends StatefulWidget {

            public final GameProfile profile;

            public FirePlayer(GameProfile profile) {this.profile = profile;}

            @Override
            public WidgetState<State.FirePlayer> createState() {
                return new State.FirePlayer.FirePlayerState();
            }

            public static class FirePlayerState extends WidgetState<State.FirePlayer> {

                private LivingEntity displayEntity;

                private boolean dead = false;

                @Override
                public void init() {
                    this.displayEntity = EntityComponent.createRenderablePlayer(this.widget().profile);
                }

                @Override
                public Widget build(BuildContext context) {
                    if (this.dead) this.displayEntity.setSharedFlagOnFire(false);
                    this.displayEntity.setHealth(dead ? 0 : 20);
                    this.displayEntity.deathTime = dead ? 20 : 0;
                    return Interactable.primary(
                        this.dead ? null : () -> {
                            this.setState(() -> {
                                this.dead = true;
                            });
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_DEATH, 1));
                            SharedState.set(
                                context, State.MurderState.class, state -> {
                                    if (!displayEntity.getUUID().equals(UUID.fromString("91a033f7-1dd3-4858-9c7b-8fb61ba6363d"))) {
                                        state.murders = state.murders.add(BigInteger.ONE);
                                    } else {
                                        state.eepies = state.eepies.add(BigInteger.ONE);
                                        state.bed = BuiltInRegistries.ITEM.getRandomElementOf(ItemTags.BEDS, RandomSource.create()).get().value().getDefaultInstance();
                                    }
                                }
                            );
                            scheduleDelayedCallback(
                                Duration.ofSeconds(displayEntity.getUUID().equals(UUID.fromString("09de8a6d-86bf-4c15-bb93-ce3384ce4e96")) ? 1 : 3), () -> this.setState(() -> {
                                    this.dead = false;
                                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.TOTEM_USE, 1));
                                })
                            );
                        },
                        widget -> widget
                            .enterCallback(!this.dead ? () -> this.displayEntity.setSharedFlagOnFire(true) : null)
                            .exitCallback(!this.dead ? () -> this.displayEntity.setSharedFlagOnFire(false) : null)
                            .cursorStyle(!this.dead ? CursorStyle.CROSSHAIR : null),
                        new Stack(
                            new StackBase(
                                new Panel(
                                    Identifier.fromNamespaceAndPath("uwu", "contributors_panel"),
                                    new Padding(
                                        Insets.top(8),
                                        new Sized(
                                            96,
                                            96,
                                            new EntityWidget(
                                                1.35, this.displayEntity, widget -> {
                                                widget.displayMode(displayEntity.isDeadOrDying() ? EntityDisplayMode.NONE : EntityDisplayMode.CURSOR);
                                                if (displayEntity.isDeadOrDying()) {
                                                    widget.transform((matrix) -> {
                                                        if (displayEntity.getUUID().equals(UUID.fromString("91a033f7-1dd3-4858-9c7b-8fb61ba6363d"))) {
                                                            matrix.translate(-1f, 1f, 0);
                                                        }

                                                        matrix.rotateX((float) Math.toRadians(0.01));
                                                    });
                                                }
                                            }
                                            )
                                        )
                                    )
                                )
                            ),
                            new Visibility(
                                this.dead && this.displayEntity.getUUID().equals(UUID.fromString("91a033f7-1dd3-4858-9c7b-8fb61ba6363d")),
                                new Stack(
                                    new Transform(
                                        new Matrix3x2f(),
                                        new ItemStackWidget(
                                            SharedState.getWithoutDependency(context, State.MurderState.class).bed,
                                            widget -> widget
                                                .displayContext(ItemDisplayContext.NONE)
                                                .transform(matrix4f -> matrix4f
                                                    .rotate(Axis.YP.rotationDegrees(90))
                                                    .rotate(Axis.ZP.rotationDegrees(15))
                                                    .scale(.45f, .45f, .45f)
                                                    .translate(0, -.45f, .45f))
                                        )
                                    ),
                                    new Align(
                                        Alignment.TOP_LEFT,
                                        new Transform(
                                            new Matrix3x2f().translation(75, 10),
                                            new Label(new LabelStyle(Alignment.TOP_LEFT, null, null, true), true, Component.literal("    z\n  z\nz"))
                                        )
                                    )
                                )
                            )
                        )
                    );
                }
            }
        }

        public static class RatingBar extends StatefulWidget {
            @Override
            public WidgetState<State.RatingBar> createState() {
                return new State.RatingBar.RatingBarState();
            }

            public static class RatingBarState extends WidgetState<State.RatingBar> {

                private int selectedStarCount = 0;
                private int hoverStarCount = 0;

                @Override
                public Widget build(BuildContext context) {
                    return new MouseArea(
                        widget -> widget
                            .exitCallback(() -> setState(() -> this.hoverStarCount = 0))
                            .cursorStyle(CursorStyle.HAND),
                        new Row(
                            this.star(0),
                            this.star(1),
                            this.star(2),
                            this.star(3),
                            this.star(4)
                        )
                    );
                }

                private Widget star(int idx) {
                    return new Interactable(
                        SHORTCUTS,
                        widget -> widget
                            .addCallbackAction(PrimaryActionIntent.class, ($, $$) -> setState(() -> this.selectedStarCount = idx + 1))
                            .addCallbackAction(SecondaryActionIntent.class, ($, $$) -> setState(() -> this.selectedStarCount = 0))
                            .enterCallback(() -> setState(() -> this.hoverStarCount = idx + 1)),
                        new Stack(
                            new SpriteWidget(
                                new Material(
                                    Identifier.parse("textures/atlas/gui.png"),
                                    Identifier.fromNamespaceAndPath("uwu", (idx + 1) <= this.selectedStarCount ? "favorite_icon_selected" : "favorite_icon")
                                )
                            ),
                            (idx + 1) <= this.hoverStarCount
                                ? new SpriteWidget(
                                new Material(
                                    Identifier.parse("textures/atlas/gui.png"),
                                    Identifier.fromNamespaceAndPath("uwu", "favorite_icon_hover")
                                )
                            ) : new Padding(Insets.none())
                        )
                    );
                }
            }
        }
    }

    private static final Map<List<ShortcutTrigger>, Intent> SHORTCUTS = Map.of(
        List.of(ShortcutTrigger.LEFT_CLICK), PrimaryActionIntent.INSTANCE,
        List.of(ShortcutTrigger.RIGHT_CLICK), SecondaryActionIntent.INSTANCE
    );
}
