package io.wispforest.owo.itemgroup.base;

import io.wispforest.owo.itemgroup.core.ContentSupplier;
import io.wispforest.owo.itemgroup.gui.ScrollerTextures;
import io.wispforest.owo.itemgroup.gui.TabTextures;
import io.wispforest.owo.itemgroup.core.ItemGroupButton;
import io.wispforest.owo.itemgroup.core.ItemGroupTab;
import io.wispforest.owo.util.pond.OwoItemGroupExtension;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

///
/// An interface for the base for owos extension on top of [ItemGroup]
///
public interface OwoItemGroup {

    BiConsumer<Item, ItemGroup.Entries> DEFAULT_STACK_GENERATOR = (item, stacks) -> stacks.add(item.getDefaultStack());

    @Nullable
    static OwoItemGroup getExtension(ItemGroup group) {
        return ((OwoItemGroupExtension) group).owo$getExtension();
    }

    //--

    /**
     * Adds the specified button to the buttons on
     * the right side of the creative menu
     *
     * @param button The button to add
     * @see ItemGroupButton#link(RegistryKey, Icon, String, String)
     * @see ItemGroupButton#curseforge(RegistryKey, String)
     * @see ItemGroupButton#discord(RegistryKey, String)
     */
    void addButton(ItemGroupButton button);

    /**
     * Adds a new tab to this group
     *
     * @param icon       The icon to use
     * @param name       The name of the tab, used for the translation key
     * @param contentTag The tag used for filling this tab
     * @param texture    The texture to use for drawing the button
     * @see Icon#of(ItemConvertible)
     */
    void addTab(Icon icon, String name, @Nullable TagKey<Item> contentTag, Identifier texture, boolean primary);

    /**
     * Adds a new tab to this group, using the default button texture
     *
     * @param icon       The icon to use
     * @param name       The name of the tab, used for the translation key
     * @param contentTag The tag used for filling this tab
     * @see Icon#of(ItemConvertible)
     */
    void addTab(Icon icon, String name, @Nullable TagKey<Item> contentTag, boolean primary);

    /**
     * Adds a new tab to this group, using the default button texture
     *
     * @param icon            The icon to use
     * @param name            The name of the tab, used for the translation key
     * @param contentSupplier The function used for filling this tab
     * @param texture         The texture to use for drawing the button
     * @see Icon#of(ItemConvertible)
     */
    void addCustomTab(Icon icon, String name, ContentSupplier contentSupplier, Identifier texture, boolean primary);

    /**
     * Adds a new tab to this group
     *
     * @param icon            The icon to use
     * @param name            The name of the tab, used for the translation key
     * @param contentSupplier The function used for filling this tab
     * @see Icon#of(ItemConvertible)
     */
    void addCustomTab(Icon icon, String name, ContentSupplier contentSupplier, boolean primary);

    void addTabs(Collection<ItemGroupTab> tabs);

    void addButtons(Collection<ItemGroupButton> buttons);

    //--

    ///
    /// @return Alternative textures for when rendering the background for the given [ItemGroup]
    ///
    @Nullable
    Identifier backgroundTexture();

    ///
    /// @return Alternative textures for when rendering the scrollbar for the given [ItemGroup]
    ///
    @Nullable
    ScrollerTextures scrollerTextures();

    ///
    /// @return Alternative textures for when rendering the tabs for the given [ItemGroup]
    ///
    @Nullable
    TabTextures tabTextures();

    ///
    /// @return The max height of tab to be stacked when ordering such when rendering the [ItemGroup]
    ///
    int tabStackHeight();

    ///
    /// @return The max height of buttons to be stacked when ordering such when rendering the [ItemGroup]
    ///
    int buttonStackHeight();

    ///
    /// Whether the given title for [ItemGroup#getDisplayName()] should be adjusted when
    /// rendering the name to show a pathed name based on the current opened tab
    ///
    boolean useDynamicTitle();

    ///
    /// @return Whether the end user has the ability to select multiple tabs at once to allow
    /// for seeing multiple tabs entries at once
    ///
    boolean allowMultiSelect();

    //--

    ///
    /// @return A replacement for [ItemGroup#getIcon()] with higher capabilities for rendering
    /// or [Icon#NONE] if not used
    ///
    Icon icon();

    List<ItemGroupTab> getTabs();

    ///
    /// @return The given [ItemGroupTab] for the following index if present
    ///
    @Nullable
    default ItemGroupTab getTab(int index) {
        var tabs = this.getTabs();
        return index < tabs.size() ? tabs.get(index) : null;
    }

    ///
    /// @return All [ItemGroupButton]s added to the given extension
    ///
    List<ItemGroupButton> getButtons();

    RegistryKey<ItemGroup> itemGroupId();
}
