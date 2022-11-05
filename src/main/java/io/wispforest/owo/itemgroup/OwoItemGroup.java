package io.wispforest.owo.itemgroup;

import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupButtonWidget;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.mixin.itemgroup.ItemGroupAccessor;
import io.wispforest.owo.util.pond.OwoItemExtensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.*;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
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

    protected static final ItemGroupTab PLACEHOLDER_TAB = new ItemGroupTab(Icon.of(Items.AIR), Text.empty(), (b, r, u) -> {}, ItemGroupTab.DEFAULT_TEXTURE, false);

    public final List<ItemGroupTab> tabs = new ArrayList<>();
    public final List<ItemGroupButton> buttons = new ArrayList<>();

    private Icon icon = null;

    private int selectedTab = 0;
    private boolean initialized = false;

    private int tabStackHeight = 4;
    private int buttonStackHeight = 4;
    private Identifier customTexture = null;
    private boolean useDynamicTitle = true;
    private boolean displaySingleTab = false;

    protected OwoItemGroup(Identifier id) {
        super(id);
    }

    /**
     * Called from {@link #initialize()} to register tabs and buttons
     *
     * @see #addTab(Icon, String, TagKey, boolean)
     * @see #addButton(ItemGroupButton)
     */
    protected abstract void setup();

    protected abstract Icon makeIcon();

    // ---------

    /**
     * Executes {@link #setup()} and makes sure this item group is ready for use
     * <p>
     * Call this after all of your items have been registered to make sure your icons
     * show up correctly
     */
    public void initialize() {
        if (this.initialized) return;

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) this.setup();
        if (tabs.size() == 0) this.tabs.add(PLACEHOLDER_TAB);

        this.initialized = true;
    }

    /**
     * Adds the specified button to the buttons on
     * the right side of the creative menu
     *
     * @param button The button to add
     *               //     * @see ItemGroupButton#link(Icon, String, String)
     *               //     * @see ItemGroupButton#curseforge(String)
     *               //     * @see ItemGroupButton#discord(String)
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
    protected void addTab(Icon icon, String name, @Nullable TagKey<Item> contentTag, Identifier texture, boolean primary) {
        this.tabs.add(new ItemGroupTab(
                icon,
                ButtonDefinition.tooltipFor(this, "tab", name),
                contentTag == null
                        ? (features, entries, hasPermissions) -> {}
                        : (features, entries, hasPermissions) -> Registry.ITEM.stream().filter(item -> item.getRegistryEntry().isIn(contentTag)).forEach(entries::add),
                texture,
                primary
        ));
    }

    /**
     * Adds a new tab to this group, using the default button texture
     *
     * @param icon       The icon to use
     * @param name       The name of the tab, used for the translation key
     * @param contentTag The tag used for filling this tab
     * @see Icon#of(ItemConvertible)
     */
    protected void addTab(Icon icon, String name, @Nullable TagKey<Item> contentTag, boolean primary) {
        addTab(icon, name, contentTag, ItemGroupTab.DEFAULT_TEXTURE, primary);
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
        this.useDynamicTitle = false;
    }

    // Getters and setters

    public void setSelectedTab(int selectedTab) {
        this.selectedTab = selectedTab;
        ((ItemGroupAccessor) this).owo$setDisplayStacks(null);
        ((ItemGroupAccessor) this).owo$searchTabStacks(null);
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

    public boolean hasDynamicTitle() {
        return this.useDynamicTitle && (this.tabs.size() > 1 || this.shouldDisplaySingleTab());
    }

    public boolean shouldDisplaySingleTab() {
        return this.displaySingleTab;
    }

    public List<ItemGroupButton> getButtons() {
        return buttons;
    }

    public ItemGroupTab getTab(int index) {
        return index < this.tabs.size() ? this.tabs.get(index) : null;
    }

    public Icon icon() {
        return this.icon == null
                ? this.icon = this.makeIcon()
                : this.icon;
    }

    // Utility

    @Override
    protected void addItems(FeatureSet enabledFeatures, Entries entries, boolean hasPermissions) {
        if (!this.initialized) throw new IllegalStateException("oωo item group not initialized, was 'initialize()' called?");
        this.getSelectedTab().contentSupplier().addItems(enabledFeatures, entries, hasPermissions);

        Registry.ITEM.stream()
                .filter(item -> ((OwoItemExtensions) item).owo$group() == this && ((OwoItemExtensions) item).owo$tab() == this.selectedTab)
                .forEach(item -> ((OwoItemExtensions) item).owo$stackGenerator().accept(item, entries));
    }

    /**
     * @deprecated Override and use {@link #makeIcon()} instead
     */
    @Override
    @Deprecated(forRemoval = true)
    public ItemStack createIcon() {
        return ItemStack.EMPTY;
    }

    /**
     * Defines a button's appearance and translation key
     * <p>
     * Used by {@link ItemGroupButtonWidget}
     */
    public interface ButtonDefinition {
        Icon icon();

        Identifier texture();

        Text tooltip();

        static Text tooltipFor(ItemGroup group, String component, String componentName) {
            var groupId = group.getId().getNamespace().equals("minecraft")
                    ? group.getId().getPath()
                    : group.getId().getNamespace() + "." + group.getId().getPath();

            return Text.translatable("itemGroup." + groupId + "." + component + "." + componentName);
        }
    }
}
