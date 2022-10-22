package io.wispforest.owo.itemgroup;

import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupButtonWidget;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.util.pond.OwoItemExtensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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
public abstract class OwoItemGroup extends FabricItemGroup {

    public static final BiConsumer<Item, Entries> DEFAULT_STACK_GENERATOR = (item, stacks) -> stacks.add(item.getDefaultStack());

    public final List<ItemGroupTab> tabs = new ArrayList<>();
    public final List<ItemGroupButton> buttons = new ArrayList<>();

    private int selectedTab = 0;
    private boolean initialized = false;

    private int tabStackHeight = 4;
    private int buttonStackHeight = 4;
    private Identifier customTexture = null;
    private boolean displayTabNamesAsTitle = true;
    private boolean displaySingleTab = false;

    protected OwoItemGroup(Identifier id) {
        super(id);
    }

    /**
     * Called from {@link #initialize()} to register tabs and buttons
     *
     * @see #addTab(Icon, String, TagKey)
     * @see #addButton(ItemGroupButton)
     */
    protected abstract void setup();

    // ---------

    /**
     * Executes {@link #setup()} and makes sure this item group is ready for use
     * <p>
     * Call this after all of your items have been registered to make sure your icons
     * show up correctly
     */
    public void initialize() {
        if (initialized) return;

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) setup();
        if (tabs.size() == 0) this.addTab(Icon.of(Items.AIR), "based_placeholder_tab", null);
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
    protected void addTab(Icon icon, String name, TagKey<Item> contentTag, Identifier texture) {
        this.tabs.add(new ItemGroupTab(icon, name, contentTag, texture));
    }

    /**
     * Adds a new tab to this group, using the default button texture
     *
     * @param icon       The icon to use
     * @param name       The name of the tab, used for the translation key
     * @param contentTag The tag used for filling this tab
     * @see Icon#of(ItemConvertible)
     */
    protected void addTab(Icon icon, String name, @Nullable TagKey<Item> contentTag) {
        addTab(icon, name, contentTag, ItemGroupTab.DEFAULT_TEXTURE);
    }

    protected void setCustomTexture(Identifier texture) {
        this.customTexture = texture;
    }

    /**
     * Sets how many tab buttons may be displayed in a single
     * column to the left of the creative inventory
     */
    protected void setTabStackHeight(int tabStackHeight) {
        this.tabStackHeight = tabStackHeight;
    }

    /**
     * Sets how many buttons may be displayed in a single
     * column to the right of the creative inventory
     */
    protected void setButtonStackHeight(int buttonStackHeight) {
        this.buttonStackHeight = buttonStackHeight;
    }

    /**
     * Display a tab button, even if only a single tab is registered
     */
    protected void displaySingleTab() {
        this.displaySingleTab = true;
    }

    /**
     * Do not change the title of the group to the name of the
     * currently selected tab - instead always the name of the group itself
     */
    protected void keepStaticTitle() {
        this.displayTabNamesAsTitle = false;
    }

    // Getters and setters

    public void setSelectedTab(int selectedTab) {
        this.selectedTab = selectedTab;
        this.clearStacks();
    }

    public ItemGroupTab getSelectedTab() {
        return tabs.get(selectedTab);
    }

    public int getSelectedTabIndex() {
        return selectedTab;
    }

    public Identifier getCustomTexture() {
        return customTexture;
    }

    public int getTabStackHeight() {
        return tabStackHeight;
    }

    public int getButtonStackHeight() {
        return buttonStackHeight;
    }

    public boolean shouldDisplayTabNamesAsTitle() {
        return displayTabNamesAsTitle && this.tabs.size() > 1;
    }

    public boolean shouldDisplaySingleTab() {
        return displaySingleTab;
    }

    public List<ItemGroupButton> getButtons() {
        return buttons;
    }

    public ItemGroupTab getTab(int index) {
        return index < this.tabs.size() ? this.tabs.get(index) : null;
    }

    // Utility

    @Override
    protected void addItems(FeatureSet enabledFeatures, Entries entries) {
        if (!initialized) throw new IllegalStateException("Owo item group not initialized, was 'initialize()' called?");
        Registry.ITEM.stream().filter(this::includes).forEach(item -> {
            ((OwoItemExtensions) item).owo$stackGenerator().accept(item, entries);
        });
    }

    protected boolean includes(Item item) {
        var group = ((OwoItemExtensions) item).owo$group();

        return tabs.size() > 1
                ? this.getSelectedTab().includes(item) || (group == this && ((OwoItemExtensions) item).owo$tab() == this.getSelectedTabIndex())
                : group == this;
    }

    /**
     * Defines a button's appearance and translation key
     * <p>
     * Used by {@link ItemGroupButtonWidget}
     */
    public interface ButtonDefinition {
        Icon icon();

        Identifier texture();

        String getTranslationKey(String groupKey);
    }
}
