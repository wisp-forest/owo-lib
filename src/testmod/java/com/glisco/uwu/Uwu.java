package com.glisco.uwu;

import com.glisco.owo.itemgroup.Icon;
import com.glisco.owo.itemgroup.OwoItemGroup;
import com.glisco.owo.itemgroup.gui.ItemGroupButton;
import com.glisco.owo.itemgroup.gui.ItemGroupTab;
import com.glisco.owo.registration.reflect.FieldRegistrationHandler;
import com.glisco.uwu.items.UwuItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tempora.dev.animawid.FrameMetadata;
import tempora.dev.animawid.SpriteSheetMetadata;

public class Uwu implements ModInitializer {

    public static final Tag<Item> TAB_2_CONTENT = TagFactory.ITEM.create(new Identifier("uwu", "tab_2_content"));
    public static final Identifier GROUP_TEXTURE = new Identifier("uwu", "textures/gui/group.png");
    public static final Identifier OWO_ICON_TEXTURE = new Identifier("uwu", "textures/gui/icon.png");

    public static final OwoItemGroup FOUR_TAB_GROUP = new OwoItemGroup(new Identifier("uwu", "four_tab_group")) {
        @Override
        protected void setup() {
            keepStaticTitle();

            addTab(Icon.of(
                    new Identifier("uwu", "textures/gui/animated_icon_test.png"),
                    new SpriteSheetMetadata(32, 32, new FrameMetadata(16, 16)),
                    1000,
                    true
            ), "tab_1", ItemGroupTab.EMPTY);

            addTab(Icon.of(Items.EMERALD), "tab_2", TAB_2_CONTENT);
            addTab(Icon.of(Items.AMETHYST_SHARD), "tab_3", ItemGroupTab.EMPTY);
            addTab(Icon.of(Items.GOLD_INGOT), "tab_4", ItemGroupTab.EMPTY);
            addTab(Icon.of(Items.IRON_INGOT), "tab_5", ItemGroupTab.EMPTY);

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

    @Override
    public void onInitialize() {

        FieldRegistrationHandler.register(UwuItems.class, "uwu", true);

        FOUR_TAB_GROUP.initialize();
        SIX_TAB_GROUP.initialize();
    }
}