package io.wispforest.uwu;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.offline.OfflineAdvancementLookup;
import io.wispforest.owo.offline.OfflineDataLookup;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import io.wispforest.owo.text.CustomTextRegistry;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import io.wispforest.owo.ui.layers.Layer;
import io.wispforest.owo.ui.layers.Layers;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.RegistryAccess;
import io.wispforest.owo.util.TagInjector;
import io.wispforest.uwu.config.BruhConfig;
import io.wispforest.uwu.config.UwuConfig;
import io.wispforest.uwu.items.UwuItems;
import io.wispforest.uwu.network.*;
import io.wispforest.uwu.text.BasedTextContent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Uwu implements ModInitializer {

    public static final boolean WE_TESTEN_HANDSHAKE = false;

    public static final TagKey<Item> TAB_2_CONTENT = TagKey.of(RegistryKeys.ITEM, new Identifier("uwu", "tab_2_content"));
    public static final Identifier GROUP_TEXTURE = new Identifier("uwu", "textures/gui/group.png");
    public static final Identifier OWO_ICON_TEXTURE = new Identifier("uwu", "textures/gui/icon.png");
    public static final Identifier ANIMATED_BUTTON_TEXTURE = new Identifier("uwu", "textures/gui/animated_icon_test.png");

    public static final ScreenHandlerType<EpicScreenHandler> EPIC_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>(EpicScreenHandler::new);

    public static final OwoItemGroup FOUR_TAB_GROUP = OwoItemGroup.builder(new Identifier("uwu", "four_tab_group"), () -> Icon.of(Items.AXOLOTL_BUCKET))
            .disableDynamicTitle()
            .buttonStackHeight(1)
            .initializer(group -> {
                group.addTab(Icon.of(ANIMATED_BUTTON_TEXTURE, 32, 1000, false), "tab_1", null, true);
                group.addTab(Icon.of(Items.EMERALD), "tab_2", TAB_2_CONTENT, false);
                group.addTab(Icon.of(Items.AMETHYST_SHARD), "tab_3", null, false);
                group.addTab(Icon.of(Items.GOLD_INGOT), "tab_4", null, false);

                group.addButton(ItemGroupButton.github(group, "https://github.com/wisp-forest/owo-lib"));
            })
            .build();

    public static final OwoItemGroup SIX_TAB_GROUP = OwoItemGroup.builder(new Identifier("uwu", "six_tab_group"), () -> Icon.of(Items.POWDER_SNOW_BUCKET))
            .tabStackHeight(3)
            .customTexture(GROUP_TEXTURE)
            .initializer(group -> {
                group.addTab(Icon.of(Items.DIAMOND), "tab_1", null, true);
                group.addTab(Icon.of(Items.EMERALD), "tab_2", null, false);
                group.addTab(Icon.of(Items.AMETHYST_SHARD), "tab_3", null, false);
                group.addTab(Icon.of(Items.GOLD_INGOT), "tab_4", null, false);
                group.addTab(Icon.of(Items.IRON_INGOT), "tab_5", null, false);
                group.addTab(Icon.of(Items.QUARTZ), "tab_6", null, false);

                group.addButton(new ItemGroupButton(group, Icon.of(OWO_ICON_TEXTURE, 0, 0, 16, 16), "owo", () -> {
                    MinecraftClient.getInstance().player.sendMessage(Text.of("oωo button pressed!"), false);
                }));
            })
            .build();

    public static final OwoItemGroup SINGLE_TAB_GROUP = OwoItemGroup.builder(new Identifier("uwu", "single_tab_group"), () -> Icon.of(OWO_ICON_TEXTURE, 0, 0, 16, 16))
            .displaySingleTab()
            .initializer(group -> group.addTab(Icon.of(Items.SPONGE), "tab_1", null, true))
            .build();

    public static final ItemGroup VANILLA_GROUP = FabricItemGroup.builder(new Identifier("uwu", "vanilla_group"))
            .icon(Items.ACACIA_BOAT::getDefaultStack)
            .entries((enabledFeatures, entries, operatorEnabled) -> entries.add(Items.MANGROVE_CHEST_BOAT))
            .build();

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(new Identifier("uwu", "uwu"));

    public static final TestMessage MESSAGE = new TestMessage("hahayes", 69, Long.MAX_VALUE, ItemStack.EMPTY, Short.MAX_VALUE, Byte.MAX_VALUE, new BlockPos(69, 420, 489),
            Float.NEGATIVE_INFINITY, Double.NaN, false, new Identifier("uowou", "hahayes"), Collections.emptyMap(),
            new int[]{10, 20}, new String[]{"trollface"}, new short[]{1, 2, 3}, new long[]{Long.MAX_VALUE, 1, 3}, new byte[]{1, 2, 3, 4},
            Optional.of("NullableString"), Optional.empty(),
            ImmutableList.of(new BlockPos(9786, 42, 9234)), new SealedSubclassOne("basede", 10), new SealedSubclassTwo(10, null));

    public static final ParticleSystemController PARTICLE_CONTROLLER = new ParticleSystemController(new Identifier("uwu", "particles"));
    public static final ParticleSystem<Void> CUBE = PARTICLE_CONTROLLER.registerDeferred(Void.class);
    public static final ParticleSystem<Void> BREAK_BLOCK_PARTICLES = PARTICLE_CONTROLLER.register(Void.class, (world, pos, data) -> {
        ClientParticles.persist();

        ClientParticles.setParticleCount(30);
        ClientParticles.spawnLine(ParticleTypes.DRAGON_BREATH, world, pos.add(.5, .5, .5), pos.add(.5, 2.5, .5), .015f);

        ClientParticles.randomizeVelocityOnAxis(.1, Direction.Axis.Z);
        ClientParticles.spawn(ParticleTypes.CLOUD, world, pos.add(.5, 2.5, .5), 0);

        ClientParticles.reset();
    });

    public static final UwuConfig CONFIG = UwuConfig.createAndLoad();
    public static final BruhConfig BRUHHHHH = BruhConfig.createAndLoad();

    @Override
    public void onInitialize() {

        FieldRegistrationHandler.register(UwuItems.class, "uwu", true);

        TagInjector.inject(Registries.BLOCK, BlockTags.BASE_STONE_OVERWORLD.id(), Blocks.GLASS);
        TagInjector.injectTagReference(Registries.ITEM, ItemTags.COALS.id(), ItemTags.FOX_FOOD.id());

        FOUR_TAB_GROUP.initialize();
        SIX_TAB_GROUP.initialize();
        SINGLE_TAB_GROUP.initialize();

        CHANNEL.registerClientbound(TestMessage.class, (message, access) -> {
            access.player().sendMessage(Text.of(message.string), false);
        });

        CHANNEL.registerClientboundDeferred(OtherTestMessage.class);

        CHANNEL.registerServerbound(TestMessage.class, (message, access) -> {
            access.player().sendMessage(Text.of(String.valueOf(message.bite)), false);
            access.player().sendMessage(Text.of(String.valueOf(message)), false);
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER && WE_TESTEN_HANDSHAKE) {
            OwoNetChannel.create(new Identifier("uwu", "server_only_channel"));
            new ParticleSystemController(new Identifier("uwu", "server_only_particles"));
        }

        System.out.println(RegistryAccess.getEntry(Registries.ITEM, Items.ACACIA_BOAT));
        System.out.println(RegistryAccess.getEntry(Registries.ITEM, new Identifier("acacia_planks")));

        Layers.push(Containers::verticalFlow, instance -> {
            instance.adapter.rootComponent.child(
                    Containers.horizontalFlow(Sizing.content(), Sizing.content())
                            .child(Components.entity(Sizing.fixed(20), EntityType.ALLAY, null).<EntityComponent<AllayEntity>>configure(component -> {
                                component.allowMouseRotation(true)
                                        .scale(.75f);

                                component.mouseDown().subscribe((mouseX, mouseY, button) -> {
                                    UISounds.playInteractionSound();
                                    return true;
                                });
                            })).child(Components.textBox(Sizing.fixed(100), "allay text").<TextFieldWidget>configure(textBox -> {
                                textBox.verticalSizing(Sizing.fixed(9));
                                textBox.setDrawsBackground(false);
                            })).<FlowLayout>configure(layout -> {
                                layout.gap(5).margins(Insets.left(4)).verticalAlignment(VerticalAlignment.CENTER);

                                instance.alignComponentToWidget(widget -> {
                                    if (!(widget instanceof ButtonWidget button)) return false;
                                    return button.getMessage().getContent() instanceof TranslatableTextContent translatable && translatable.getKey().equals("menu.reportBugs");
                                }, Layer.Instance.AnchorSide.RIGHT, 0, layout);
                            })
            );
        }, GameMenuScreen.class);

        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            dispatcher.register(
                    literal("show_nbt")
                            .then(argument("player", GameProfileArgumentType.gameProfile())
                                    .executes(context -> {
                                        GameProfile profile = GameProfileArgumentType.getProfileArgument(context, "player").iterator().next();
                                        NbtCompound tag = OfflineDataLookup.get(profile.getId());
                                        context.getSource().sendFeedback(NbtHelper.toPrettyPrintedText(tag), false);
                                        return 0;
                                    })));

            dispatcher.register(
                    literal("test_advancement_cache")
                            .then(literal("read")
                                    .then(argument("player", GameProfileArgumentType.gameProfile())
                                            .executes(context -> {
                                                GameProfile profile = GameProfileArgumentType.getProfileArgument(context, "player").iterator().next();
                                                Map<Identifier, AdvancementProgress> map = OfflineAdvancementLookup.get(profile.getId());
                                                context.getSource().sendFeedback(Text.literal(map.toString()), false);
                                                System.out.println(map);
                                                return 0;
                                            })))
                            .then(literal("write")
                                    .then(argument("player", GameProfileArgumentType.gameProfile())
                                            .executes(context -> {
                                                MinecraftServer server = context.getSource().getServer();
                                                GameProfile profile = GameProfileArgumentType.getProfileArgument(context, "player").iterator().next();

                                                OfflineAdvancementLookup.edit(profile.getId(), handle -> {
                                                    handle.grant(server.getAdvancementLoader().get(new Identifier("story/iron_tools")));
                                                });

                                                return 0;
                                            }))));

        });

        CustomTextRegistry.register("based", BasedTextContent.Serializer.INSTANCE);

        UwuNetworkExample.init();
        UwuOptionalNetExample.init();
    }

    public record OtherTestMessage(BlockPos pos, String message) {}

    public record TestMessage(String string, Integer integer, Long along, ItemStack stack, Short ashort, Byte bite,
                              BlockPos pos, Float afloat, Double adouble, Boolean aboolean, Identifier identifier,
                              Map<String, Integer> map,
                              int[] arr1, String[] arr2, short[] arr3, long[] arr4, byte[] arr5,
                              Optional<String> optional1, Optional<String> optional2,
                              List<BlockPos> posses, SealedTestClass sealed1, SealedTestClass sealed2) {}

}