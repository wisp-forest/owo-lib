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
import io.wispforest.owo.util.RegistryAccess;
import io.wispforest.owo.util.TagInjector;
import io.wispforest.uwu.config.BruhConfig;
import io.wispforest.uwu.config.UwuConfig;
import io.wispforest.uwu.items.UwuItems;
import io.wispforest.uwu.network.*;
import io.wispforest.uwu.text.BasedTextContent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Uwu implements ModInitializer {

    public static final boolean WE_TESTEN_HANDSHAKE = false;

    public static final TagKey<Item> TAB_2_CONTENT = TagKey.of(Registry.ITEM_KEY, new Identifier("uwu", "tab_2_content"));
    public static final Identifier GROUP_TEXTURE = new Identifier("uwu", "textures/gui/group.png");
    public static final Identifier OWO_ICON_TEXTURE = new Identifier("uwu", "textures/gui/icon.png");
    public static final Identifier ANIMATED_BUTTON_TEXTURE = new Identifier("uwu", "textures/gui/animated_icon_test.png");

    public static final OwoItemGroup FOUR_TAB_GROUP = new OwoItemGroup(new Identifier("uwu", "four_tab_group")) {
        @Override
        protected void setup() {
            keepStaticTitle();

            addTab(Icon.of(ANIMATED_BUTTON_TEXTURE, 32, 1000, true), "tab_1", null);
            addTab(Icon.of(Items.EMERALD), "tab_2", TAB_2_CONTENT);
            addTab(Icon.of(Items.AMETHYST_SHARD), "tab_3", null);
            addTab(Icon.of(Items.GOLD_INGOT), "tab_4", null);

            addButton(ItemGroupButton.github("https://github.com/glisco03/owo-lib"));

            setButtonStackHeight(1);
        }

        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.AXOLOTL_BUCKET);
        }
    };

    public static final OwoItemGroup SIX_TAB_GROUP = new OwoItemGroup(new Identifier("uwu", "six_tab_group")) {
        @Override
        protected void setup() {
            setTabStackHeight(3);
            setCustomTexture(GROUP_TEXTURE);

            addTab(Icon.of(Items.DIAMOND), "tab_1", null);
            addTab(Icon.of(Items.EMERALD), "tab_2", null);
            addTab(Icon.of(Items.AMETHYST_SHARD), "tab_3", null);
            addTab(Icon.of(Items.GOLD_INGOT), "tab_4", null);
            addTab(Icon.of(Items.IRON_INGOT), "tab_5", null);
            addTab(Icon.of(Items.QUARTZ), "tab_6", null);

            addButton(new ItemGroupButton(Icon.of(OWO_ICON_TEXTURE, 0, 0, 16, 16), "owo", () -> {
                MinecraftClient.getInstance().player.sendMessage(Text.of("oωo button pressed!"), false);
            }));
        }

        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.POWDER_SNOW_BUCKET);
        }
    };

    public static final OwoItemGroup SINGLE_TAB_GROUP = new OwoItemGroup(new Identifier("uwu", "single_tab_group")) {
        @Override
        protected void setup() {
            displaySingleTab();
            addTab(Icon.of(Items.SPONGE), "tab_1", null);
        }

        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.SPYGLASS);
        }
    };

    public static final ItemGroup VANILLA_GROUP = FabricItemGroupBuilder.build(new Identifier("uwu", "vanilla_group"), Items.ACACIA_BOAT::getDefaultStack);

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

        TagInjector.inject(Registry.BLOCK, BlockTags.BASE_STONE_OVERWORLD.id(), Blocks.GLASS);
        TagInjector.injectTagReference(Registry.ITEM, ItemTags.COALS.id(), ItemTags.FOX_FOOD.id());

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

        System.out.println(RegistryAccess.getEntry(Registry.ITEM, Items.ACACIA_BOAT));
        System.out.println(RegistryAccess.getEntry(Registry.ITEM, new Identifier("acacia_planks")));

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

    public record OtherTestMessage(BlockPos pos, String message) {
    }

    public record TestMessage(String string, Integer integer, Long along, ItemStack stack, Short ashort, Byte bite,
                              BlockPos pos, Float afloat, Double adouble, Boolean aboolean, Identifier identifier,
                              Map<String, Integer> map,
                              int[] arr1, String[] arr2, short[] arr3, long[] arr4, byte[] arr5,
                              Optional<String> optional1, Optional<String> optional2,
                              List<BlockPos> posses, SealedTestClass sealed1, SealedTestClass sealed2) {
    }

}