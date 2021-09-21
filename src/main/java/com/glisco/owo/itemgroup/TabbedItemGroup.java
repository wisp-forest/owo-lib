package com.glisco.owo.itemgroup;

import com.glisco.owo.itemgroup.gui.ItemGroupButton;
import com.glisco.owo.itemgroup.gui.ItemGroupTab;
import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Shoutouts to Lemonszz for originally writing this for Biome Makeover.
 * Adapted from Azagwens implementation of it.
 *
 * @author Noaaan
 * @author glisco
 */
public abstract class TabbedItemGroup extends ItemGroup {

    public final List<ItemGroupTab> tabs = new ArrayList<>();
    private final List<ItemGroupButton> buttons = new ArrayList<>();
    private int selectedTab = 0;

    protected TabbedItemGroup(Identifier id) {
        super(createTabIndex(), String.format("%s.%s", id.getNamespace(), id.getPath()));
        prepare();
    }

    protected TabbedItemGroup(int index, String name) {
        super(index, name);
        prepare();
    }

    @Override
    public void appendStacks(DefaultedList<ItemStack> stacks) {
        Registry.ITEM.stream().filter(this::includes).forEach(item -> stacks.add(new ItemStack(item)));
    }

    private boolean includes(Item item) {
        if (tabs.size() > 1)
            return getSelectedTab().includes(item) || ((OwoItemExtensions) item).getTab() == this.getSelectedTab();
        else
            return Objects.equals(item.getGroup().getName(), this.getName());
    }

    public void setSelectedTab(int selectedTab) {
        this.selectedTab = selectedTab;
    }

    public ItemGroupTab getSelectedTab() {
        return tabs.get(selectedTab);
    }

    public int getSelectedTabIndex() {
        return selectedTab;
    }

    public List<ItemGroupButton> getButtons() {
        return buttons;
    }

    public ItemGroupTab getTab(int index) {
        return index < this.tabs.size() ? this.tabs.get(index) : null;
    }

    private static int createTabIndex() {
        ((ItemGroupExtensions) ItemGroup.BUILDING_BLOCKS).fabric_expandArray();
        return ItemGroup.GROUPS.length - 1;
    }

    private void prepare() {
        setup();
        if (tabs.size() == 0) this.addTab(Icon.of(Items.AIR), "based_placeholder_tab", Tag.of(Set.of()));
    }

    protected abstract void setup();

    protected void addButton(ItemGroupButton button) {
        this.buttons.add(button);
    }

    protected void addTab(Icon icon, String name, Tag<Item> contentTag, Identifier texture) {
        this.tabs.add(new ItemGroupTab(icon, name, contentTag, texture));
    }

    protected void addTab(Icon icon, String name, Tag<Item> contentTag) {
        addTab(icon, name, contentTag, ItemGroupTab.DEFAULT_TEXTURE);
    }

    public interface DrawableComponent {
        Icon icon();

        Identifier texture();

        String getTranslationKey(String groupKey);
    }
}
