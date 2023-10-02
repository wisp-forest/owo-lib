package io.wispforest.owo.itemgroup;

import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupButtonWidget;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.mixin.itemgroup.ItemGroupAccessor;
import io.wispforest.owo.util.pond.OwoItemExtensions;
import it.unimi.dsi.fastutil.ints.*;
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
 * Extensions for  {@link ItemGroup} which support multiple sub-tabs
 * within, as well as arbitrary buttons with defaults provided for links
 * to places like GitHub, Modrinth, etc.
 * <p>
 * Tabs can be populated by using {@link OwoItemSettings} and setting the
 * {@link OwoItemSettings#tab(int)}. Furthermore, tags can be used for easily populating
 * tabs from data
 * <p>
 * The roots of this implementation originated in Biome Makeover, where it was written by Lemonszz
 */
public abstract class OwoItemGroup extends ItemGroup {

    public static final BiConsumer<Item, Entries> DEFAULT_STACK_GENERATOR = (item, stacks) -> stacks.add(item.getDefaultStack());

    protected static final ItemGroupTab PLACEHOLDER_TAB = new ItemGroupTab(Icon.of(Items.AIR), Text.empty(), (br, uh) -> {}, ItemGroupTab.DEFAULT_TEXTURE, false);

    public final List<ItemGroupTab> tabs = new ArrayList<>();
    public final List<ItemGroupButton> buttons = new ArrayList<>();

    private final Consumer<OwoItemGroup> initializer;

    private final Supplier<Icon> iconSupplier;
    private Icon icon;

    private final IntSet activeTabs = new IntAVLTreeSet(IntComparators.NATURAL_COMPARATOR);
    private final IntSet activeTabsView = IntSets.unmodifiable(this.activeTabs);
    private boolean initialized = false;

    private final @Nullable Identifier backgroundTexture;
    private final @Nullable ScrollerTextures scrollerTextures;
    private final @Nullable TabTextures tabTextures;

    private final int tabStackHeight;
    private final int buttonStackHeight;
    private final boolean useDynamicTitle;
    private final boolean displaySingleTab;
    private final boolean allowMultiSelect;

    protected OwoItemGroup(Identifier id, Consumer<OwoItemGroup> initializer, Supplier<Icon> iconSupplier, int tabStackHeight, int buttonStackHeight, @Nullable Identifier backgroundTexture, @Nullable ScrollerTextures scrollerTextures, @Nullable TabTextures tabTextures, boolean useDynamicTitle, boolean displaySingleTab, boolean allowMultiSelect) {
        super(null, -1, Type.CATEGORY, Text.translatable("itemGroup.%s.%s".formatted(id.getNamespace(), id.getPath())), () -> ItemStack.EMPTY, (displayContext, entries) -> {});
        this.initializer = initializer;
        this.iconSupplier = iconSupplier;
        this.tabStackHeight = tabStackHeight;
        this.buttonStackHeight = buttonStackHeight;
        this.backgroundTexture = backgroundTexture;
        this.scrollerTextures = scrollerTextures;
        this.tabTextures = tabTextures;
        this.useDynamicTitle = useDynamicTitle;
        this.displaySingleTab = displaySingleTab;
        this.allowMultiSelect = allowMultiSelect;

        ((ItemGroupAccessor) this).owo$setEntryCollector((context, entries) -> {
            if (!this.initialized) {
                throw new IllegalStateException("oωo item group not initialized, was 'initialize()' called?");
            }

            this.activeTabs.forEach(tabIdx -> {
                this.tabs.get(tabIdx).contentSupplier().addItems(context, entries);
                this.collectItemsFromRegistry(entries, tabIdx);
            });
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
        if (this.tabs.isEmpty()) this.tabs.add(PLACEHOLDER_TAB);

        if (this.allowMultiSelect) {
            for (int tabIdx = 0; tabIdx < this.tabs.size(); tabIdx++) {
                if (!this.tabs.get(tabIdx).primary()) continue;
                this.activeTabs.add(tabIdx);
            }

            if (this.activeTabs.isEmpty()) this.activeTabs.add(0);
        } else {
            this.activeTabs.add(0);
        }

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

        this.collectItemsFromRegistry(searchEntries, -1);
        this.tabs.forEach(tab -> tab.contentSupplier().addItems(context, searchEntries));

        ((ItemGroupAccessor) this).owo$setSearchTabStacks(searchEntries.searchTabStacks);
    }

    protected void collectItemsFromRegistry(Entries entries, int tab) {
        Registries.ITEM.stream()
                .filter(item -> ((OwoItemExtensions) item).owo$group() == this && (tab < 0 || tab == ((OwoItemExtensions) item).owo$tab()))
                .forEach(item -> ((OwoItemExtensions) item).owo$stackGenerator().accept(item, entries));
    }

    // Getters and setters

    /**
     * @deprecated Use {@link #selectSingleTab(int, DisplayContext)} instead
     */
    @Deprecated(forRemoval = true)
    public void setSelectedTab(int selectedTab, DisplayContext context) {
        this.selectSingleTab(selectedTab, context);
    }

    /**
     * @deprecated On an item group which allows multiple selection, this
     * returns the first selected tab only. Instead, call {@link #selectedTabs()}
     */
    @Deprecated(forRemoval = true)
    public ItemGroupTab getSelectedTab() {
        return this.tabs.get(this.getSelectedTabIndex());
    }

    /**
     * @deprecated On an item group which allows multiple selection, this
     * returns the index of the first selected tab only. Instead, call {@link #selectedTabs()}
     */
    @Deprecated(forRemoval = true)
    public int getSelectedTabIndex() {
        return this.activeTabs.iterator().nextInt();
    }

    /**
     * Select only {@code tab}, deselecting all other tabs,
     * using {@code context} for re-population
     */
    public void selectSingleTab(int tab, DisplayContext context) {
        this.activeTabs.clear();
        this.activeTabs.add(tab);

        this.updateEntries(context);
    }

    /**
     * Select {@code tab} in addition to other currently selected
     * tabs, using {@code context} for re-population.
     * <p>
     * If this group does not allow multiple selection, behaves
     * like {@link #selectSingleTab(int, DisplayContext)}
     */
    public void selectTab(int tab, DisplayContext context) {
        if (!this.allowMultiSelect) {
            this.activeTabs.clear();
        }

        this.activeTabs.add(tab);
        this.updateEntries(context);
    }

    /**
     * Deselect {@code tab} if it is currently selected, using {@code context} for
     * re-population. If this results in no tabs being selected, all tabs are
     * automatically selected instead
     */
    public void deselectTab(int tab, DisplayContext context) {
        if (!this.allowMultiSelect) return;

        this.activeTabs.remove(tab);
        if (this.activeTabs.isEmpty()) {
            for (int tabIdx = 0; tabIdx < this.tabs.size(); tabIdx++) {
                this.activeTabs.add(tabIdx);
            }
        }

        this.updateEntries(context);
    }

    /**
     * Shorthand for {@link #selectTab(int, DisplayContext)} or
     * {@link #deselectTab(int, DisplayContext)}, depending on the tabs
     * current state
     */
    public void toggleTab(int tab, DisplayContext context) {
        if (this.isTabSelected(tab)) {
            this.deselectTab(tab, context);
        } else {
            this.selectTab(tab, context);
        }
    }

    /**
     * @return A set containing the indices of all currently
     * selected tabs
     */
    public IntSet selectedTabs() {
        return this.activeTabsView;
    }

    /**
     * @return {@code true} if {@code tab} is currently selected
     */
    public boolean isTabSelected(int tab) {
        return this.activeTabs.contains(tab);
    }

    public @Nullable Identifier getBackgroundTexture() {
        return this.backgroundTexture;
    }

    public @Nullable ScrollerTextures getScrollerTextures() {
        return this.scrollerTextures;
    }

    public @Nullable TabTextures getTabTextures() {
        return this.tabTextures;
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

    public boolean canSelectMultipleTabs() {
        return this.allowMultiSelect;
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
        private @Nullable Identifier backgroundTexture = null;
        private @Nullable ScrollerTextures scrollerTextures = null;
        private @Nullable TabTextures tabTextures = null;
        private boolean useDynamicTitle = true;
        private boolean displaySingleTab = false;
        private boolean allowMultiSelect = true;

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

        public Builder backgroundTexture(@Nullable Identifier backgroundTexture) {
            this.backgroundTexture = backgroundTexture;
            return this;
        }

        public Builder scrollerTextures(ScrollerTextures scrollerTextures) {
            this.scrollerTextures = scrollerTextures;
            return this;
        }

        public Builder tabTextures(TabTextures tabTextures) {
            this.tabTextures = tabTextures;
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

        public Builder withoutMultipleSelection() {
            this.allowMultiSelect = false;
            return this;
        }

        public OwoItemGroup build() {
            final var group = new OwoItemGroup(id, initializer, iconSupplier, tabStackHeight, buttonStackHeight, backgroundTexture, scrollerTextures, tabTextures, useDynamicTitle, displaySingleTab, allowMultiSelect) {};
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

    public record ScrollerTextures(Identifier enabled, Identifier disabled) {}
    public record TabTextures(Identifier topSelected, Identifier topSelectedFirstColumn, Identifier topUnselected, Identifier bottomSelected, Identifier bottomSelectedFirstColumn, Identifier bottomUnselected) {}

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
