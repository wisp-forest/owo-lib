package io.wispforest.uwu;

import blue.endless.jankson.JsonPrimitive;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import io.wispforest.owo.config.ConfigSynchronizer;
import io.wispforest.owo.config.Option;
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
import io.wispforest.owo.serialization.BuiltInEndecs;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.impl.bytebuf.ByteBufDeserializer;
import io.wispforest.owo.serialization.impl.bytebuf.ByteBufSerializer;
import io.wispforest.owo.serialization.impl.json.JsonDeserializer;
import io.wispforest.owo.serialization.impl.json.JsonEndec;
import io.wispforest.owo.serialization.impl.json.JsonSerializer;
import io.wispforest.owo.serialization.impl.nbt.NbtDeserializer;
import io.wispforest.owo.serialization.impl.nbt.NbtEndec;
import io.wispforest.owo.serialization.impl.nbt.NbtSerializer;
import io.wispforest.owo.text.CustomTextRegistry;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.util.RegistryAccess;
import io.wispforest.owo.util.TagInjector;
import io.wispforest.uwu.config.BruhConfig;
import io.wispforest.uwu.config.UwuConfig;
import io.wispforest.uwu.items.UwuItems;
import io.wispforest.uwu.network.*;
import io.wispforest.uwu.recipe.UwuShapedRecipe;
import io.wispforest.uwu.text.BasedTextContent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
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
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Uwu implements ModInitializer {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean WE_TESTEN_HANDSHAKE = false;

    public static final TagKey<Item> TAB_2_CONTENT = TagKey.of(RegistryKeys.ITEM, new Identifier("uwu", "tab_2_content"));
    public static final Identifier GROUP_TEXTURE = new Identifier("uwu", "textures/gui/group.png");
    public static final Identifier OWO_ICON_TEXTURE = new Identifier("uwu", "textures/gui/icon.png");
    public static final Identifier ANIMATED_BUTTON_TEXTURE = new Identifier("uwu", "textures/gui/animated_icon_test.png");

    public static final ScreenHandlerType<EpicScreenHandler> EPIC_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>(EpicScreenHandler::new, FeatureFlags.VANILLA_FEATURES);

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
            .backgroundTexture(GROUP_TEXTURE)
            .scrollerTextures(new OwoItemGroup.ScrollerTextures(new Identifier("uwu", "scroller"), new Identifier("uwu", "scroller_disabled")))
            .tabTextures(new OwoItemGroup.TabTextures(
                    new Identifier("uwu", "top_selected"),
                    new Identifier("uwu", "top_selected_first_column"),
                    new Identifier("uwu", "top_unselected"),
                    new Identifier("uwu", "bottom_selected"),
                    new Identifier("uwu", "bottom_selected_first_column"),
                    new Identifier("uwu", "bottom_unselected")))
            .initializer(group -> {
                group.addTab(Icon.of(Items.DIAMOND), "tab_1", null, true);
                group.addTab(Icon.of(Items.EMERALD), "tab_2", null, false);
                group.addTab(Icon.of(Items.AMETHYST_SHARD), "tab_3", null, false);
                group.addTab(Icon.of(Items.GOLD_INGOT), "tab_4", null, false);
                group.addCustomTab(Icon.of(Items.IRON_INGOT), "tab_5", (context, entries) -> entries.add(UwuItems.SCREEN_SHARD), false);
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

    public static final ItemGroup VANILLA_GROUP = Registry.register(Registries.ITEM_GROUP, new Identifier("uwu", "vanilla_group"), FabricItemGroup.builder()
            .displayName(Text.literal("who did this"))
            .icon(Items.ACACIA_BOAT::getDefaultStack)
            .entries((context, entries) -> entries.add(Items.MANGROVE_CHEST_BOAT))
            .build());

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
    public static final BruhConfig BRUHHHHH = BruhConfig.createAndLoad(builder -> {
        builder.registerSerializer(Color.class, (color, marshaller) -> new JsonPrimitive("bruv"));
    });

    @Override
    public void onInitialize() {

        var stackEndec = Endec.ofCodec(ItemStack.CODEC);
        var stackData = """
                        {
                            "id": "minecraft:shroomlight",
                            "Count": 42,
                            "tag": {
                                "Enchantments": [{"id": "unbreaking", "lvl": 3}]
                            }
                        }
                """;

        var stacknite = stackEndec.decode(JsonDeserializer.of(new Gson().fromJson(stackData, JsonObject.class)));
        System.out.println(stacknite);

        var serializer = ByteBufSerializer.packet();
        stackEndec.encode(serializer, stacknite);

        System.out.println(stackEndec.decode(new ByteBufDeserializer(serializer.result())));
        System.out.println(BuiltInEndecs.BLOCK_POS.codec().encodeStart(NbtOps.INSTANCE, new BlockPos(34, 35, 69)).result().get());

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

        UwuShapedRecipe.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            dispatcher.register(
                    literal("show_nbt")
                            .then(argument("player", GameProfileArgumentType.gameProfile())
                                    .executes(context -> {
                                        GameProfile profile = GameProfileArgumentType.getProfileArgument(context, "player").iterator().next();
                                        NbtCompound tag = OfflineDataLookup.get(profile.getId());
                                        context.getSource().sendFeedback(() -> NbtHelper.toPrettyPrintedText(tag), false);
                                        return 0;
                                    })));

            dispatcher.register(
                    literal("test_advancement_cache")
                            .then(literal("read")
                                    .then(argument("player", GameProfileArgumentType.gameProfile())
                                            .executes(context -> {
                                                GameProfile profile = GameProfileArgumentType.getProfileArgument(context, "player").iterator().next();
                                                Map<Identifier, AdvancementProgress> map = OfflineAdvancementLookup.get(profile.getId());
                                                context.getSource().sendFeedback(() -> Text.literal(map.toString()), false);
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

            dispatcher.register(literal("get_option")
                    .then(argument("config", StringArgumentType.string())
                            .then(argument("option", StringArgumentType.string()).executes(context -> {
                                var value = ConfigSynchronizer.getClientOptions(
                                        context.getSource().getPlayer(),
                                        StringArgumentType.getString(context, "config")
                                ).get(new Option.Key(StringArgumentType.getString(context, "option")));

                                context.getSource().sendFeedback(() -> Text.literal(String.valueOf(value)), false);

                                return 0;
                            }))));

            dispatcher.register(literal("kodeck_test")
                    .executes(context -> {
                        var rand = context.getSource().getWorld().random;
                        var source = context.getSource();

                        //--

                        String testPhrase = "This is a test to see how kodeck dose.";

                        LOGGER.info("Input:  " + testPhrase);

                        var nbtData = Endec.STRING.encodeFully(NbtSerializer::of, testPhrase);
                        var fromNbtData = Endec.STRING.decodeFully(NbtDeserializer::of, nbtData);

                        var jsonData = Endec.STRING.encodeFully(JsonSerializer::of, fromNbtData);
                        var fromJsonData = Endec.STRING.decodeFully(JsonDeserializer::of, jsonData);

                        LOGGER.info("Output: " + fromJsonData);

                        LOGGER.info("");

                        //--

                        int randomNumber = rand.nextInt(20000);

                        LOGGER.info("Input:  " + randomNumber);

                        var jsonNum = Endec.INT.encodeFully(JsonSerializer::of, randomNumber);

                        LOGGER.info("Output: " + Endec.INT.decodeFully(JsonDeserializer::of, jsonNum));

                        LOGGER.info("");

                        //--

                        List<Integer> randomNumbers = new ArrayList<>();

                        var maxCount = rand.nextInt(20);

                        for(int i = 0; i < maxCount; i++){
                            randomNumbers.add(rand.nextInt(20000));
                        }

                        LOGGER.info("Input:  " + randomNumbers);

                        Endec<List<Integer>> INT_LIST_KODECK = Endec.INT.listOf();

                        var nbtListData = INT_LIST_KODECK.encodeFully(NbtSerializer::of, randomNumbers);

                        LOGGER.info("Output: " + INT_LIST_KODECK.decodeFully(NbtDeserializer::of, nbtListData));

                        LOGGER.info("");

                        //---

                        if (source.getPlayer() == null) return 0;

                        ItemStack handStack = source.getPlayer().getStackInHand(Hand.MAIN_HAND);

                        LOGGER.info(handStack.toString());
                        LOGGER.info(handStack.getOrCreateNbt().asString().replace("\n", "\\n"));

                        LOGGER.info("---");

                        JsonElement stackJsonData;

                        try {
                            stackJsonData = BuiltInEndecs.ITEM_STACK.encodeFully(JsonSerializer::of, handStack);
                        } catch (Exception exception){
                            LOGGER.info(exception.getMessage());
                            LOGGER.info((Arrays.toString(exception.getStackTrace())));

                            return 0;
                        }

                        LOGGER.info(stackJsonData.toString());

                        LOGGER.info("---");

                        try {
                            handStack = BuiltInEndecs.ITEM_STACK.decodeFully(JsonDeserializer::of, stackJsonData);
                        } catch (Exception exception){
                            LOGGER.info(exception.getMessage());
                            LOGGER.info((Arrays.toString(exception.getStackTrace())));

                            return 0;
                        }

                        LOGGER.info(handStack.toString());
                        LOGGER.info(handStack.getOrCreateNbt().asString().replace("\n", "\\n"));

                        LOGGER.info("");

                        //--

                        {
                            LOGGER.info("--- Format Based Endec Test");

                            var nbtDataStack = handStack.getOrCreateNbt();

                            LOGGER.info("  Input:  " + nbtDataStack.asString().replace("\n", "\\n"));

                            var jsonDataStack = NbtEndec.ELEMENT.encodeFully(JsonSerializer::of, nbtDataStack);

                            LOGGER.info("  Json:  " + jsonDataStack);

                            var convertedNbtDataStack = NbtEndec.ELEMENT.decodeFully(JsonDeserializer::of, jsonDataStack);

                            LOGGER.info("Output:  " + convertedNbtDataStack.asString().replace("\n", "\\n"));

                            LOGGER.info("---");

                            LOGGER.info("");
                        }

                        //--

                        {
                            LOGGER.info("--- Transpose Format Based Endec Test");

                            var nbtDataStack = handStack.getOrCreateNbt();

                            LOGGER.info("  Input:  " + nbtDataStack.asString().replace("\n", "\\n"));

                            var jsonDataStack = NbtEndec.ELEMENT.encodeFully(JsonSerializer::of, nbtDataStack);

                            LOGGER.info("  Json:  " + jsonDataStack);

                            var convertedNbtDataStack = JsonEndec.INSTANCE.encodeFully(NbtSerializer::of, jsonDataStack);

                            LOGGER.info("Output:  " + convertedNbtDataStack.asString().replace("\n", "\\n"));

                            LOGGER.info("---");

                            LOGGER.info("");
                        }

                        //--

                        {
                            var variable1Endec = Endec.STRING.keyed("variable1", "");
                            var variable2Endec = Endec.INT.keyed("variable2", 0);
                            var variable3Endec = TestRecord.ENDEC.keyed("variable3Endec", null);

                            var variable1 = "Weeeeeee";
                            var variable2 = 1000;
                            var variable3 = new TestRecord("Matt", 24, List.of("One", "Two", "Three", "Four"));

                            LOGGER.info(variable1);
                            LOGGER.info(String.valueOf(variable2));
                            LOGGER.info(String.valueOf(variable3));

                            NbtCompound compound = new NbtCompound();

                            compound.put(variable1Endec, variable1);
                            compound.put(variable2Endec, variable2);
                            compound.put(variable3Endec, variable3);

                            LOGGER.info("");
                            LOGGER.info(compound.asString());

                            LOGGER.info("");

                            LOGGER.info(compound.get(variable1Endec));
                            LOGGER.info(compound.get(variable2Endec).toString());
                            LOGGER.info(compound.get(variable3Endec).toString());

                            LOGGER.info("---");
                            LOGGER.info("");
                        }

                        //--



                        //--

                        //Vanilla
                        iterations("Vanilla", (buf) -> {
                            ItemStack stack = source.getPlayer().getStackInHand(Hand.MAIN_HAND);

                            var stackFromByte = buf.writeItemStack(stack).readItemStack();
                        });

                        //Codeck
                        try {
                            iterations("Endec", (buf) -> {
                                ItemStack stack = source.getPlayer().getStackInHand(Hand.MAIN_HAND);

                                BuiltInEndecs.ITEM_STACK.encode(new ByteBufSerializer<>(buf), stack);

                                var stackFromByte = BuiltInEndecs.ITEM_STACK.decodeFully(ByteBufDeserializer::new, buf);
                            });
                        } catch (Exception exception){
                            LOGGER.info(exception.getMessage());
                            LOGGER.info(Arrays.toString(exception.getStackTrace()));

                            return 0;
                        }

                        return 0;
                    }));
        });

        CustomTextRegistry.register("based", BasedTextContent.Serializer.INSTANCE);

        UwuNetworkExample.init();
        UwuOptionalNetExample.init();
    }

    private static void iterations(String label, Consumer<PacketByteBuf> action){
        int maxTrials = 3;
        int maxIterations = 50;

        List<Long> durations = new ArrayList<>();

        LOGGER.info("-----");
        LOGGER.info(label);

        for (int trial = 0; trial < maxTrials; trial++) {
            durations.clear();

            for (int i = 0; i < maxIterations; i++) {
                PacketByteBuf buf = PacketByteBufs.create();

                long startTime = System.nanoTime();

                action.accept(buf);

                durations.add(System.nanoTime() - startTime);
            }

            LOGGER.info(String.format(maxIterations + " Trials took on average: %.2f", ((durations.stream().mapToLong(v -> v).sum()) / (double) durations.size()) / 1000000));
        }

        LOGGER.info("-----");

    }

    public record TestRecord(String name, int count, List<String> names) {
        public static final Endec<TestRecord> ENDEC = StructEndecBuilder.of(
                Endec.STRING.fieldOf("name", TestRecord::name),
                Endec.INT.fieldOf("count", TestRecord::count),
                Endec.STRING.listOf().fieldOf("names", TestRecord::names),
                TestRecord::new
        );
    }

    public record OtherTestMessage(BlockPos pos, String message) {}

    public record TestMessage(String string, Integer integer, Long along, ItemStack stack, Short ashort, Byte bite,
                              BlockPos pos, Float afloat, Double adouble, Boolean aboolean, Identifier identifier,
                              Map<String, Integer> map,
                              int[] arr1, String[] arr2, short[] arr3, long[] arr4, byte[] arr5,
                              Optional<String> optional1, Optional<String> optional2,
                              List<BlockPos> posses, SealedTestClass sealed1, SealedTestClass sealed2) {}
}