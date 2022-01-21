package io.wispforest.uwu;

import com.google.common.collect.ImmutableList;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.network.annotations.CollectionType;
import io.wispforest.owo.network.annotations.MapTypes;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.system.ParticleSystem;
import io.wispforest.owo.particles.system.ParticleSystemManager;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import io.wispforest.owo.util.TagInjector;
import io.wispforest.uwu.items.UwuItems;
import io.wispforest.uwu.network.UwuNetworkExample;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Uwu implements ModInitializer {

    public static final Tag<Item> TAB_2_CONTENT = TagFactory.ITEM.create(new Identifier("uwu", "tab_2_content"));
    public static final Identifier GROUP_TEXTURE = new Identifier("uwu", "textures/gui/group.png");
    public static final Identifier OWO_ICON_TEXTURE = new Identifier("uwu", "textures/gui/icon.png");
    public static final Identifier ANIMATED_BUTTON_TEXTURE = new Identifier("uwu", "textures/gui/animated_icon_test.png");

    public static final OwoItemGroup FOUR_TAB_GROUP = new OwoItemGroup(new Identifier("uwu", "four_tab_group")) {
        @Override
        protected void setup() {
            keepStaticTitle();

            addTab(Icon.of(ANIMATED_BUTTON_TEXTURE, 32, 1000, true), "tab_1", ItemGroupTab.EMPTY);
            addTab(Icon.of(Items.EMERALD), "tab_2", TAB_2_CONTENT);
            addTab(Icon.of(Items.AMETHYST_SHARD), "tab_3", ItemGroupTab.EMPTY);
            addTab(Icon.of(Items.GOLD_INGOT), "tab_4", ItemGroupTab.EMPTY);

            addButton(ItemGroupButton.github("https://github.com/glisco03/owo-lib"));
        }

        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.AXOLOTL_BUCKET);
        }
    };

    public static final OwoItemGroup SIX_TAB_GROUP = new OwoItemGroup(new Identifier("uwu", "six_tab_group")) {
        @Override
        protected void setup() {
            setStackHeight(6);
            setCustomTexture(GROUP_TEXTURE);

            addTab(Icon.of(Items.DIAMOND), "tab_1", ItemGroupTab.EMPTY);
            addTab(Icon.of(Items.EMERALD), "tab_2", ItemGroupTab.EMPTY);
            addTab(Icon.of(Items.AMETHYST_SHARD), "tab_3", ItemGroupTab.EMPTY);
            addTab(Icon.of(Items.GOLD_INGOT), "tab_4", ItemGroupTab.EMPTY);
            addTab(Icon.of(Items.IRON_INGOT), "tab_5", ItemGroupTab.EMPTY);
            addTab(Icon.of(Items.QUARTZ), "tab_6", ItemGroupTab.EMPTY);

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
            addTab(Icon.of(Items.SPONGE), "tab_1", ItemGroupTab.EMPTY);
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
            ImmutableList.of(new BlockPos(9786, 42, 9234)));

    public static final ParticleSystemManager PARTICLE_MANAGER = new ParticleSystemManager(new Identifier("uwu", "particles"));
    public static final ParticleSystem<Void> CUBE = PARTICLE_MANAGER.register(Void.class, (world, pos, data) -> {
        ClientParticles.setParticleCount(5);
        ClientParticles.spawnCubeOutline(ParticleTypes.END_ROD, world, pos, 1, .01f);
    });
    public static final ParticleSystem<Void> BREAK_BLOCK_PARTICLES = PARTICLE_MANAGER.register(Void.class, (world, pos, data) -> {
        ClientParticles.persist();

        ClientParticles.setParticleCount(30);
        ClientParticles.spawnLine(ParticleTypes.DRAGON_BREATH, world, pos.add(.5, .5, .5), pos.add(.5, 2.5, .5), .015f);

        ClientParticles.randomizeVelocity(.1);
        ClientParticles.spawn(ParticleTypes.CLOUD, world, pos.add(.5, 2.5, .5), 0);

        ClientParticles.reset();
    });

    @Override
    public void onInitialize() {

        FieldRegistrationHandler.register(UwuItems.class, "uwu", true);

        TagInjector.injectBlocks(new Identifier("base_stone_overworld"), Blocks.GLASS);

        FOUR_TAB_GROUP.initialize();
        SIX_TAB_GROUP.initialize();
        SINGLE_TAB_GROUP.initialize();

        CHANNEL.registerClientbound(TestMessage.class, (message, access) -> {
            access.player().sendMessage(Text.of(message.string), false);
        });

        CHANNEL.registerClientbound(OtherTestMessage.class, (message, access) -> {
            access.player().sendMessage(Text.of("Message '" + message.message + "' from " + message.pos), false);
        });

        CHANNEL.registerServerbound(TestMessage.class, (message, access) -> {
            access.player().sendMessage(Text.of(String.valueOf(message.bite)), false);
        });

        UwuNetworkExample.init();
    }

    public record OtherTestMessage(BlockPos pos, String message) {}

    public record TestMessage(String string, Integer integer, Long along, ItemStack stack, Short ashort, Byte bite,
                              BlockPos pos, Float afloat, Double adouble, Boolean aboolean, Identifier identifier,
                              @MapTypes(keys = String.class, values = Integer.class) Map<String, Integer> map,
                              @CollectionType(BlockPos.class) List<BlockPos> posses) {}

}