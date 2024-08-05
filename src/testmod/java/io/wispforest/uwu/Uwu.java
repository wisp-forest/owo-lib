package io.wispforest.uwu;

import blue.endless.jankson.JsonPrimitive;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import io.wispforest.endec.format.gson.GsonDeserializer;
import io.wispforest.endec.format.gson.GsonEndec;
import io.wispforest.endec.format.gson.GsonSerializer;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
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
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.Endec;
import io.wispforest.endec.format.bytebuf.ByteBufSerializer;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.serialization.format.nbt.NbtDeserializer;
import io.wispforest.owo.serialization.format.nbt.NbtEndec;
import io.wispforest.owo.serialization.format.nbt.NbtSerializer;
import io.wispforest.owo.text.CustomTextRegistry;
import io.wispforest.owo.ui.core.Color;
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
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Uwu implements ModInitializer {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean WE_TESTEN_HANDSHAKE = false;

    public static final TagKey<Item> TAB_2_CONTENT = TagKey.of(Registries.ITEM, Identifier.of("uwu", "tab_2_content"));
    public static final Identifier GROUP_TEXTURE = Identifier.of("uwu", "textures/gui/group.png");
    public static final Identifier OWO_ICON_TEXTURE = Identifier.of("uwu", "textures/gui/icon.png");
    public static final Identifier ANIMATED_BUTTON_TEXTURE = Identifier.of("uwu", "textures/gui/animated_icon_test.png");

    public static final MenuType<EpicScreenHandler> EPIC_SCREEN_HANDLER_TYPE = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.of("uwu", "epic_screen_handler"),
        new MenuType<>(EpicScreenHandler::new, FeatureFlags.VANILLA_SET)
    );

    public static final OwoItemGroup FOUR_TAB_GROUP = OwoItemGroup.builder(Identifier.of("uwu", "four_tab_group"), () -> Icon.of(Items.AXOLOTL_BUCKET))
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

    public static final OwoItemGroup SIX_TAB_GROUP = OwoItemGroup.builder(Identifier.of("uwu", "six_tab_group"), () -> Icon.of(Items.POWDER_SNOW_BUCKET))
            .tabStackHeight(3)
            .backgroundTexture(GROUP_TEXTURE)
            .scrollerTextures(new OwoItemGroup.ScrollerTextures(Identifier.of("uwu", "scroller"), Identifier.of("uwu", "scroller_disabled")))
            .tabTextures(new OwoItemGroup.TabTextures(
                    Identifier.of("uwu", "top_selected"),
                    Identifier.of("uwu", "top_selected_first_column"),
                    Identifier.of("uwu", "top_unselected"),
                    Identifier.of("uwu", "bottom_selected"),
                    Identifier.of("uwu", "bottom_selected_first_column"),
                    Identifier.of("uwu", "bottom_unselected")))
            .initializer(group -> {
                group.addTab(Icon.of(Items.DIAMOND), "tab_1", null, true);
                group.addTab(Icon.of(Items.EMERALD), "tab_2", null, false);
                group.addTab(Icon.of(Items.AMETHYST_SHARD), "tab_3", null, false);
                group.addTab(Icon.of(Items.GOLD_INGOT), "tab_4", null, false);
                group.addCustomTab(Icon.of(Items.IRON_INGOT), "tab_5", (context, entries) -> entries.accept(UwuItems.SCREEN_SHARD), false);
                group.addTab(Icon.of(Items.QUARTZ), "tab_6", null, false);

                group.addButton(new ItemGroupButton(group, Icon.of(OWO_ICON_TEXTURE, 0, 0, 16, 16), "owo", () -> {
                    Minecraft.getInstance().player.displayClientMessage(Text.literal("oωo button pressed!"), false);
                }));
            })
            .build();

    public static final OwoItemGroup SINGLE_TAB_GROUP = OwoItemGroup.builder(Identifier.of("uwu", "single_tab_group"), () -> Icon.of(OWO_ICON_TEXTURE, 0, 0, 16, 16))
            .displaySingleTab()
            .initializer(group -> group.addTab(Icon.of(Items.SPONGE), "tab_1", null, true))
            .build();

    public static final CreativeModeTab VANILLA_GROUP = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.of("uwu", "vanilla_group"), FabricItemGroup.builder()
            .title(Text.literal("who did this"))
            .icon(Items.ACACIA_BOAT::getDefaultInstance)
            .displayItems((context, entries) -> entries.accept(Items.MANGROVE_CHEST_BOAT))
            .build());

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(Identifier.of("uwu", "uwu"));

    public static final TestMessage MESSAGE = new TestMessage("hahayes", 69, Long.MAX_VALUE, ItemStack.EMPTY, Short.MAX_VALUE, Byte.MAX_VALUE, new BlockPos(69, 420, 489),
            Float.NEGATIVE_INFINITY, Double.NaN, false, Identifier.of("uowou", "hahayes"), Collections.emptyMap(),
            new int[]{10, 20}, new String[]{"trollface"}, new short[]{1, 2, 3}, new long[]{Long.MAX_VALUE, 1, 3}, new byte[]{1, 2, 3, 4},
            Optional.of("NullableString"), Optional.empty(),
            ImmutableList.of(new BlockPos(9786, 42, 9234)), new SealedSubclassOne("basede", 10), new SealedSubclassTwo(10, null));

    public static final ParticleSystemController PARTICLE_CONTROLLER = new ParticleSystemController(Identifier.of("uwu", "particles"));
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

        var stackEndec = CodecUtils.toEndec(ItemStack.CODEC);
        var stackData = """
                        {
                            "id": "minecraft:shroomlight",
                            "Count": 42,
                            "tag": {
                                "Enchantments": [{"id": "unbreaking", "lvl": 3}]
                            }
                        }
                """;

        var stacknite = stackEndec.decode(SerializationContext.empty(), GsonDeserializer.of(new Gson().fromJson(stackData, JsonObject.class)));
        System.out.println(stacknite);

        var serializer = ByteBufSerializer.of(PacketByteBufs.create());
        stackEndec.encode(SerializationContext.empty(), serializer, stacknite);

        System.out.println(serializer.result().read(SerializationContext.empty(), stackEndec));
        System.out.println(CodecUtils.toCodec(MinecraftEndecs.BLOCK_POS).encodeStart(NbtOps.INSTANCE, new BlockPos(34, 35, 69)).result().get());

        FieldRegistrationHandler.register(UwuItems.class, "uwu", true);

        TagInjector.inject(BuiltInRegistries.BLOCK, BlockTags.BASE_STONE_OVERWORLD.id(), Blocks.GLASS);
        TagInjector.injectTagReference(BuiltInRegistries.ITEM, ItemTags.COALS.id(), ItemTags.FOX_FOOD.id());

        FOUR_TAB_GROUP.initialize();
        SIX_TAB_GROUP.initialize();
        SINGLE_TAB_GROUP.initialize();

        CHANNEL.registerClientbound(TestMessage.class, (message, access) -> {
            access.player().displayClientMessage(Text.literal(message.string), false);
        });

        CHANNEL.registerClientboundDeferred(OtherTestMessage.class);

        CHANNEL.registerServerbound(TestMessage.class, (message, access) -> {
            access.player().sendSystemMessage(Text.literal(String.valueOf(message.bite)), false);
            access.player().sendSystemMessage(Text.literal(String.valueOf(message)), false);
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER && WE_TESTEN_HANDSHAKE) {
            OwoNetChannel.create(Identifier.of("uwu", "server_only_channel"));
            new ParticleSystemController(Identifier.of("uwu", "server_only_particles"));
        }

        System.out.println(RegistryAccess.getEntry(BuiltInRegistries.ITEM, Items.ACACIA_BOAT));
        System.out.println(RegistryAccess.getEntry(BuiltInRegistries.ITEM, Identifier.parse("acacia_planks")));

//        UwuShapedRecipe.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            dispatcher.register(
                    literal("show_nbt")
                            .then(argument("player", GameProfileArgument.gameProfile())
                                    .executes(context -> {
                                        GameProfile profile = GameProfileArgument.getGameProfiles(context, "player").iterator().next();
                                        NbtCompound tag = OfflineDataLookup.get(profile.getId());
                                        context.getSource().sendSuccess(() -> NbtUtils.toPrettyComponent(tag), false);
                                        return 0;
                                    })));

            dispatcher.register(
                    literal("test_advancement_cache")
                            .then(literal("read")
                                    .then(argument("player", GameProfileArgument.gameProfile())
                                            .executes(context -> {
                                                GameProfile profile = GameProfileArgument.getGameProfiles(context, "player").iterator().next();
                                                Map<Identifier, AdvancementProgress> map = OfflineAdvancementLookup.get(profile.getId());
                                                context.getSource().sendSuccess(() -> Text.literal(map.toString()), false);
                                                System.out.println(map);
                                                return 0;
                                            })))
                            .then(literal("write")
                                    .then(argument("player", GameProfileArgument.gameProfile())
                                            .executes(context -> {
                                                MinecraftServer server = context.getSource().getServer();
                                                GameProfile profile = GameProfileArgument.getGameProfiles(context, "player").iterator().next();

                                                OfflineAdvancementLookup.edit(profile.getId(), handle -> {
                                                    handle.grant(server.getAdvancements().get(Identifier.parse("story/iron_tools")));
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

                                context.getSource().sendSuccess(() -> Text.literal(String.valueOf(value)), false);

                                return 0;
                            }))));

            dispatcher.register(literal("kodeck_test")
                    .executes(context -> {
                        var rand = context.getSource().getLevel().random;
                        var source = context.getSource();

                        //--

                        String testPhrase = "This is a test to see how kodeck dose.";

                        LOGGER.info("Input:  " + testPhrase);

                        var nbtData = Endec.STRING.encodeFully(NbtSerializer::of, testPhrase);
                        var fromNbtData = Endec.STRING.decodeFully(NbtDeserializer::of, nbtData);

                        var jsonData = Endec.STRING.encodeFully(GsonSerializer::of, fromNbtData);
                        var fromJsonData = Endec.STRING.decodeFully(GsonDeserializer::of, jsonData);

                        LOGGER.info("Output: " + fromJsonData);

                        LOGGER.info("");

                        //--

                        int randomNumber = rand.nextInt(20000);

                        LOGGER.info("Input:  " + randomNumber);

                        var jsonNum = Endec.INT.encodeFully(GsonSerializer::of, randomNumber);

                        LOGGER.info("Output: " + Endec.INT.decodeFully(GsonDeserializer::of, jsonNum));

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

                        ItemStack handStack = source.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);

                        LOGGER.info(handStack.toString());
                        LOGGER.info(handStack.getComponents().toString().replace("\n", "\\n"));

                        LOGGER.info("---");

                        JsonElement stackJsonData;

                        try {
                            stackJsonData = MinecraftEndecs.ITEM_STACK.encodeFully(SerializationContext.attributes(RegistriesAttribute.of(context.getSource().getLevel().registryAccess())), GsonSerializer::of, handStack);
                        } catch (Exception exception){
                            LOGGER.info(exception.getMessage());
                            LOGGER.info((Arrays.toString(exception.getStackTrace())));

                            return 0;
                        }

                        LOGGER.info(stackJsonData.toString());

                        LOGGER.info("---");

                        try {
                            handStack = MinecraftEndecs.ITEM_STACK.decodeFully(SerializationContext.attributes(RegistriesAttribute.of(context.getSource().getLevel().registryAccess())), GsonDeserializer::of, stackJsonData);
                        } catch (Exception exception){
                            LOGGER.info(exception.getMessage());
                            LOGGER.info((Arrays.toString(exception.getStackTrace())));

                            return 0;
                        }

                        LOGGER.info(handStack.toString());
                        LOGGER.info(handStack.getComponents().toString().replace("\n", "\\n"));

                        LOGGER.info("");

                        //--

                        {
                            LOGGER.info("--- Format Based Endec Test");

                            var nbtDataStack = handStack.save(access);

                            LOGGER.info("  Input:  " + nbtDataStack.asString().replace("\n", "\\n"));

                            var jsonDataStack = NbtEndec.ELEMENT.encodeFully(GsonSerializer::of, nbtDataStack);

                            LOGGER.info("  Json:  " + jsonDataStack);

                            var convertedNbtDataStack = NbtEndec.ELEMENT.decodeFully(GsonDeserializer::of, jsonDataStack);

                            LOGGER.info("Output:  " + convertedNbtDataStack.asString().replace("\n", "\\n"));

                            LOGGER.info("---");

                            LOGGER.info("");
                        }

                        //--

                        {
                            LOGGER.info("--- Transpose Format Based Endec Test");

                            var nbtDataStack = handStack.save(access);

                            LOGGER.info("  Input:  " + nbtDataStack.asString().replace("\n", "\\n"));

                            var jsonDataStack = NbtEndec.ELEMENT.encodeFully(GsonSerializer::of, nbtDataStack);

                            LOGGER.info("  Json:  " + jsonDataStack);

                            var convertedNbtDataStack = GsonEndec.INSTANCE.encodeFully(NbtSerializer::of, jsonDataStack);

                            LOGGER.info("Output:  " + convertedNbtDataStack.asString().replace("\n", "\\n"));

                            LOGGER.info("---");

                            LOGGER.info("");
                        }

                        //--

                        {
                            var variable1Endec = Endec.STRING.keyed("variable1", "");
                            var variable2Endec = Endec.INT.keyed("variable2", 0);
                            var variable3Endec = TestRecord.ENDEC.keyed("variable3Endec", (TestRecord) null);

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
                            ItemStack stack = source.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);

                            ItemStack.STREAM_CODEC.encode(buf, stack);
                            var stackFromByte = ItemStack.STREAM_CODEC.decode(buf);
                        });

                        //Codeck
                        try {
                            iterations("Endec", (buf) -> {
                                ItemStack stack = source.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
                                buf.write(SerializationContext.attributes(RegistriesAttribute.of(context.getSource().getLevel().registryAccess())), MinecraftEndecs.ITEM_STACK, stack);

                                var stackFromByte = buf.read(SerializationContext.attributes(RegistriesAttribute.of(context.getSource().getLevel().registryAccess())), MinecraftEndecs.ITEM_STACK);
                            });
                        } catch (Exception exception){
                            LOGGER.info(exception.getMessage());
                            LOGGER.info(Arrays.toString(exception.getStackTrace()));

                            return 0;
                        }

                        return 0;
                    }));
        });

        CustomTextRegistry.register(BasedTextContent.TYPE, "based");

        UwuNetworkExample.init();
        UwuOptionalNetExample.init();
    }

    private static void iterations(String label, Consumer<RegistryFriendlyByteBuf> action){
        int maxTrials = 3;
        int maxIterations = 50;

        List<Long> durations = new ArrayList<>();

        LOGGER.info("-----");
        LOGGER.info(label);

        for (int trial = 0; trial < maxTrials; trial++) {
            durations.clear();

            for (int i = 0; i < maxIterations; i++) {
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), Owo.currentServer().registryAccess());

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