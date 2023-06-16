package io.wispforest.owo.itemgroup;

import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupButtonWidget;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.mixin.itemgroup.ItemGroupAccessor;
import io.wispforest.owo.util.pond.OwoItemExtensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    public static final BiConsumer<Item, Entries> DEFAULT_STACK_GENERATOR = (item, stacks) -> stacks.add(item.getDefaultStack());

    protected static final ItemGroupTab PLACEHOLDER_TAB = new ItemGroupTab(Icon.of(Items.AIR), Text.empty(), (br, uh) -> {}, ItemGroupTab.DEFAULT_TEXTURE, false);

    public final List<ItemGroupTab> tabs = new ArrayList<>();
    public final List<ItemGroupButton> buttons = new ArrayList<>();

    private final Identifier id;
    private final Consumer<OwoItemGroup> initializer;

    private final Supplier<Icon> iconSupplier;
    private Icon icon;

    private int selectedTab = 0;
    private boolean initialized = false;

    private final int tabStackHeight;
    private final int buttonStackHeight;
    private final Identifier customTexture;
    private final boolean useDynamicTitle;
    private final boolean displaySingleTab;

    protected OwoItemGroup(Identifier id, Consumer<OwoItemGroup> initializer, Supplier<Icon> iconSupplier, int tabStackHeight, int buttonStackHeight, @Nullable Identifier customTexture, boolean useDynamicTitle, boolean displaySingleTab) {
        super(null, -1, Type.CATEGORY, Text.translatable("itemGroup.%s.%s".formatted(id.getNamespace(), id.getPath())), () -> ItemStack.EMPTY, (displayContext, entries) -> {});
        this.id = id;
        this.initializer = initializer;
        this.iconSupplier = iconSupplier;
        this.tabStackHeight = tabStackHeight;
        this.buttonStackHeight = buttonStackHeight;
        this.customTexture = customTexture;
        this.useDynamicTitle = useDynamicTitle;
        this.displaySingleTab = displaySingleTab;

        ((ItemGroupAccessor) this).owo$setEntryCollector((context, entries) -> {
            if (!this.initialized) {
                throw new IllegalStateException("oωo item group not initialized, was 'initialize()' called?");
            }
            this.getSelectedTab().contentSupplier().addItems(context, entries);
            this.collectItemsFromRegistry(entries, true);
        });
    }

    public static Builder builder(Identifier id, Supplier<Icon> iconSupplier) {
        return new Builder(id, iconSupplier);
    }

    // ---------

    /**
     * Executes {@link #initializer} and makes sure this item group is ready for use
     * <p>
     * Call this after all of your items have been registered to make sure your icons
     * show up correctly
     */
    public void initialize() {
        if (this.initialized) return;

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) this.initializer.accept(this);
        if (tabs.size() == 0) this.tabs.add(PLACEHOLDER_TAB);

        this.initialized = true;
    }

    /**
     * Adds the specified button to the buttons on
     * the right side of the creative menu
     *
     * @param button The button to add
     * @see ItemGroupButton#link(ItemGroup, Icon, String, String)
     * @see ItemGroupButton#curseforge(ItemGroup, String)
     * @see ItemGroupButton#discord(ItemGroup, String)
     */
    public void addButton(ItemGroupButton button) {
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
    public void addTab(Icon icon, String name, @Nullable TagKey<Item> contentTag, Identifier texture, boolean primary) {
        this.tabs.add(new ItemGroupTab(
                icon,
                ButtonDefinition.tooltipFor(this, "tab", name),
                contentTag == null
                        ? (context, entries) -> {}
                        : (context, entries) -> Registries.ITEM.stream().filter(item -> item.getRegistryEntry().isIn(contentTag)).forEach(entries::add),
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
    public void addTab(Icon icon, String name, @Nullable TagKey<Item> contentTag, boolean primary) {
        addTab(icon, name, contentTag, ItemGroupTab.DEFAULT_TEXTURE, primary);
    }

    /**
     * Adds a new tab to this group, using the default button texture
     *
     * @param icon            The icon to use
     * @param name            The name of the tab, used for the translation key
     * @param contentSupplier The function used for filling this tab
     * @param texture         The texture to use for drawing the button
     * @see Icon#of(ItemConvertible)
     */
    public void addCustomTab(Icon icon, String name, ItemGroupTab.ContentSupplier contentSupplier, Identifier texture, boolean primary) {
        this.tabs.add(new ItemGroupTab(
                icon,
                ButtonDefinition.tooltipFor(this, "tab", name),
                contentSupplier, texture, primary
        ));
    }

    /**
     * Adds a new tab to this group
     *
     * @param icon            The icon to use
     * @param name            The name of the tab, used for the translation key
     * @param contentSupplier The function used for filling this tab
     * @see Icon#of(ItemConvertible)
     */
    public void addCustomTab(Icon icon, String name, ItemGroupTab.ContentSupplier contentSupplier, boolean primary) {
        this.addCustomTab(icon, name, contentSupplier, ItemGroupTab.DEFAULT_TEXTURE, primary);
    }

    @Override
    public void updateEntries(DisplayContext context) {
        super.updateEntries(context);

        var searchEntries = new SearchOnlyEntries(this, context.enabledFeatures());

        this.collectItemsFromRegistry(searchEntries, false);
        this.tabs.forEach(tab -> tab.contentSupplier().addItems(context, searchEntries));

        ((ItemGroupAccessor) this).owo$setSearchTabStacks(searchEntries.searchTabStacks);
    }

    protected void collectItemsFromRegistry(Entries entries, boolean matchTab) {
        Registries.ITEM.stream()
                .filter(item -> ((OwoItemExtensions) item).owo$group() == this && (!matchTab || ((OwoItemExtensions) item).owo$tab() == this.selectedTab))
                .forEach(item -> ((OwoItemExtensions) item).owo$stackGenerator().accept(item, entries));
    }

    // Getters and setters

    public void setSelectedTab(int selectedTab, DisplayContext context) {
        this.selectedTab = selectedTab;
        this.updateEntries(context);
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
                ? this.icon = this.iconSupplier.get()
                : this.icon;
    }

    @Override
    public boolean shouldDisplay() {
        return true;
    }

    public Identifier id() {
        return Registries.ITEM_GROUP.getId(this);
    }

    public static class Builder {

        private final Identifier id;
        private final Supplier<Icon> iconSupplier;

        private Consumer<OwoItemGroup> initializer = owoItemGroup -> {};
        private int tabStackHeight = 4;
        private int buttonStackHeight = 4;
        private @Nullable Identifier customTexture = null;
        private boolean useDynamicTitle = true;
        private boolean displaySingleTab = false;

        private Builder(Identifier id, Supplier<Icon> iconSupplier) {
            this.id = id;
            this.iconSupplier = iconSupplier;
        }

        public Builder initializer(Consumer<OwoItemGroup> initializer) {
            this.initializer = initializer;
            return this;
        }

        public Builder tabStackHeight(int tabStackHeight) {
            this.tabStackHeight = tabStackHeight;
            return this;
        }

        public Builder buttonStackHeight(int buttonStackHeight) {
            this.buttonStackHeight = buttonStackHeight;
            return this;
        }

        public Builder customTexture(@Nullable Identifier customTexture) {
            this.customTexture = customTexture;
            return this;
        }

        public Builder disableDynamicTitle() {
            this.useDynamicTitle = false;
            return this;
        }

        public Builder displaySingleTab() {
            this.displaySingleTab = true;
            return this;
        }

        public OwoItemGroup build() {
            final var group = new OwoItemGroup(id, initializer, iconSupplier, tabStackHeight, buttonStackHeight, customTexture, useDynamicTitle, displaySingleTab) {};
            Registry.register(Registries.ITEM_GROUP, this.id, group);
            return group;
        }
    }

    protected static class SearchOnlyEntries extends EntriesImpl {

        public SearchOnlyEntries(ItemGroup group, FeatureSet enabledFeatures) {
            super(group, enabledFeatures);
        }

        @Override
        public void add(ItemStack stack, StackVisibility visibility) {
            if (visibility == StackVisibility.PARENT_TAB_ONLY) return;
            super.add(stack, StackVisibility.SEARCH_TAB_ONLY);
        }
    }

    // Utility

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
            var registryId = Registries.ITEM_GROUP.getId(group);
            var groupId = registryId.getNamespace().equals("minecraft")
                    ? registryId.getPath()
                    : registryId.getNamespace() + "." + registryId.getPath();

            return Text.translatable("itemGroup." + groupId + "." + component + "." + componentName);
        }

    }
}
