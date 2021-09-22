package com.glisco.owo.itemgroup;

import com.glisco.owo.itemgroup.gui.ItemGroupButton;
import com.glisco.owo.itemgroup.gui.ItemGroupTab;
import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;
import net.minecraft.item.*;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A custom implementation of {@link ItemGroup} that supports multiple sub-tabs
 * within itself, as well as arbitrary buttons with defaults provided for links
 * like GitHub, Modrinth, etc.
 * <p>
 * By default, Items are added via tags, however you can also use {@link OwoItemSettings}
 * and set the tab for a given item via {@link OwoItemSettings#tab(int)}
 * <p>
 * Credits to Lemonszz for originally writing this for Biome Makeover.
 * Adapted from Azagwens implementation
 */
public abstract class OwoItemGroup extends ItemGroup {

    public final List<ItemGroupTab> tabs = new ArrayList<>();
    private final List<ItemGroupButton> buttons = new ArrayList<>();

    private int selectedTab = 0;
    private boolean initialized = false;

    /**
     * Creates a new instance. This also automatically registers the group
     * (basically like calling {@code build()} on Fabric's builder),
     * so be careful when and how you invoke this
     *
     * @param id The id this group should use. Will be formatted as {@code <namespace>.<path>}
     */
    protected OwoItemGroup(Identifier id) {
        super(createTabIndex(), String.format("%s.%s", id.getNamespace(), id.getPath()));
    }

    /**
     * Creates a new instance from the given name at the given index, without ensuring that
     * there is space in the array or the name is valid. Used by {@link com.glisco.owo.itemgroup.json.WrapperGroup}
     * to replace an existing group
     *
     * @apiNote This should not be used from the outside, unless there is a very specific need to
     */
    @ApiStatus.Internal
    protected OwoItemGroup(int index, String name) {
        super(index, name);
    }

    /**
     * Executes {@link #setup()} and makes sure this item group is ready for use
     * <p>
     * Call this after all of your items have been registered to make sure your icons
     * show up correctly
     */
    public void initialize() {
        if (initialized) return;

        setup();
        if (tabs.size() == 0) this.addTab(Icon.of(Items.AIR), "based_placeholder_tab", Tag.of(Set.of()));
        this.initialized = true;
    }

    /**
     * Adds the specified button to the buttons on
     * the right side of the creative menu
     *
     * @param button The button to add
     * @see ItemGroupButton#link(Icon, String, String)
     * @see ItemGroupButton#curseforge(String)
     * @see ItemGroupButton#discord(String)
     */
    protected void addButton(ItemGroupButton button) {
        this.buttons.add(button);
    }

    /**
     * Adds a new tab to this group
     *
     * @param icon       The icon to use
     * @param name       The name of the tab, used for the translation key
     * @param contentTag The tag used for filling this tab
     * @param texture    The texture to use for drawing the button
     * @see Icon#of(ItemConvertible)
     */
    protected void addTab(Icon icon, String name, Tag<Item> contentTag, Identifier texture) {
        this.tabs.add(new ItemGroupTab(icon, name, contentTag, texture));
    }

    /**
     * Adds a new tab to this group, using the default button texture
     *
     * @param icon       The icon to use
     * @param name       The name of the, used for the translation key
     * @param contentTag The tag used for filling this tab
     * @see Icon#of(ItemConvertible)
     */
    protected void addTab(Icon icon, String name, Tag<Item> contentTag) {
        addTab(icon, name, contentTag, ItemGroupTab.DEFAULT_TEXTURE);
    }

    /**
     * Called from the constructor to register tabs and buttons
     *
     * @see #addTab(Icon, String, Tag)
     * @see #addButton(ItemGroupButton)
     */
    protected abstract void setup();

    // Getters and setters

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

    // Utility

    @Override
    public void appendStacks(DefaultedList<ItemStack> stacks) {
        if (!initialized) throw new IllegalStateException("Owo item group not initialized, was 'initialize()' called?");
        Registry.ITEM.stream().filter(this::includes).forEach(item -> stacks.add(new ItemStack(item)));
    }

    private boolean includes(Item item) {
        if (tabs.size() > 1)
            return getSelectedTab().includes(item) || ((OwoItemExtensions) item).getTab() == this.getSelectedTabIndex();
        else
            return item.getGroup() != null && Objects.equals(item.getGroup().getName(), this.getName());
    }

    private static int createTabIndex() {
        ((ItemGroupExtensions) ItemGroup.BUILDING_BLOCKS).fabric_expandArray();
        return ItemGroup.GROUPS.length - 1;
    }

    /**
     * Defines a button's appearance and translation key
     * <p>
     * Used by {@link com.glisco.owo.itemgroup.gui.ItemGroupButtonWidget}
     */
    public interface ButtonDefinition {
        Icon icon();

        Identifier texture();

        String getTranslationKey(String groupKey);
    }
}
