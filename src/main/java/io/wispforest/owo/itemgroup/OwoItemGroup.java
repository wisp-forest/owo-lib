package io.wispforest.owo.itemgroup;

import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupButtonWidget;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.itemgroup.json.WrapperGroup;
import io.wispforest.owo.util.pond.OwoItemExtensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.*;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
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

    public static final BiConsumer<Item, DefaultedList<ItemStack>> DEFAULT_STACK_GENERATOR = (item, stacks) -> stacks.add(item.getDefaultStack());

    protected static final ItemGroupTab PLACEHOLDER_TAB = new ItemGroupTab(Icon.of(Items.AIR), Text.empty(), (bruh) -> {}, ItemGroupTab.DEFAULT_TEXTURE, false);

    public final List<ItemGroupTab> tabs = new ArrayList<>();
    public final List<ItemGroupButton> buttons = new ArrayList<>();

    private int selectedTab = 0;
    private boolean initialized = false;

    private int tabStackHeight = 4;
    private int buttonStackHeight = 4;
    private Identifier customTexture = null;
    private boolean useDynamicTitle = true;
    private boolean displaySingleTab = false;

    protected OwoItemGroup(Identifier id) {
        super(createTabIndex(), String.format("%s.%s", id.getNamespace(), id.getPath()));
    }

    /**
     * Creates a new instance from the given name at the given index, without ensuring that
     * there is space in the array or the name is valid. Used by {@link WrapperGroup}
     * to replace an existing group
     *
     * @apiNote This should not be used from the outside, unless there is a very specific need to
     */
    @ApiStatus.Internal
    protected OwoItemGroup(int index, String name) {
        super(index, name);
    }

    public static Builder builder(Identifier id, Supplier<ItemStack> iconSupplier) {
        return new Builder(id, iconSupplier);
    }

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
     * @see ItemGroupButton#link(Icon, String, String)
     * @see ItemGroupButton#curseforge(String)
     * @see ItemGroupButton#discord(String)
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
                        ? (stacks) -> {}
                        : (stacks) -> Registry.ITEM.stream().filter(item -> item.getRegistryEntry().isIn(contentTag)).map(Item::getDefaultStack).forEach(stacks::add),
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
     * Adds a new tab to this group
     *
     * @param icon       The icon to use
     * @param name       The name of the tab, used for the translation key
     * @param contentTag The tag used for filling this tab
     * @see Icon#of(ItemConvertible)
     */
    @Deprecated
    public void addTab(Icon icon, String name, @Nullable TagKey<Item> contentTag, Identifier texture) {
        addTab(icon, name, contentTag, texture, false);
    }

    /**
     * Adds a new tab to this group, using the default button texture
     *
     * @param icon       The icon to use
     * @param name       The name of the tab, used for the translation key
     * @param contentTag The tag used for filling this tab
     * @see Icon#of(ItemConvertible)
     */
    @Deprecated
    public void addTab(Icon icon, String name, @Nullable TagKey<Item> contentTag) {
        addTab(icon, name, contentTag, ItemGroupTab.DEFAULT_TEXTURE, false);
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

    /**
     * Called from {@link #initialize()} to register tabs and buttons
     *
     * @see #addTab(Icon, String, TagKey)
     * @see #addButton(ItemGroupButton)
     */
    protected abstract void setup();

    @Override
    public void appendStacks(DefaultedList<ItemStack> stacks) {
        if (!this.initialized) throw new IllegalStateException("oωo item group not initialized, was 'initialize()' called?");
        this.getSelectedTab().contentSupplier().accept(stacks);

        Registry.ITEM.stream()
                .filter(item -> item.getGroup() == this && ((OwoItemExtensions) item).owo$tab() == this.selectedTab)
                .forEach(item -> ((OwoItemExtensions) item).owo$stackGenerator().accept(item, stacks));
    }

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

    // Utility

    private static int createTabIndex() {
        ((ItemGroupExtensions) ItemGroup.BUILDING_BLOCKS).fabric_expandArray();
        return ItemGroup.GROUPS.length - 1;
    }

    public static class Builder {

        private final Identifier id;
        private final Supplier<ItemStack> iconSupplier;

        private Consumer<OwoItemGroup> initializer = owoItemGroup -> {};
        private int tabStackHeight = 4;
        private int buttonStackHeight = 4;
        private @Nullable Identifier customTexture = null;
        private boolean useDynamicTitle = true;
        private boolean displaySingleTab = false;

        private Builder(Identifier id, Supplier<ItemStack> iconSupplier) {
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
            return new OwoItemGroup(id) {
                @Override
                public ItemStack createIcon() {
                    return iconSupplier.get();
                }

                @Override
                protected void setup() {
                    initializer.accept(this);
                    this.setTabStackHeight(Builder.this.tabStackHeight);
                    this.setButtonStackHeight(Builder.this.buttonStackHeight);
                    this.setCustomTexture(Builder.this.customTexture);

                    if (!Builder.this.useDynamicTitle) this.keepStaticTitle();
                    if (!Builder.this.displaySingleTab) this.displaySingleTab();
                }
            };
        }
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

        static Text tooltipFor(ItemGroup group, String component, String componentName) {
            var groupTranslationKey = group.getDisplayName().getContent() instanceof TranslatableTextContent translatable
                    ? translatable.getKey()
                    : group.getDisplayName().getString();

            return Text.translatable(groupTranslationKey + "." + component + "." + componentName);
        }

    }
}
