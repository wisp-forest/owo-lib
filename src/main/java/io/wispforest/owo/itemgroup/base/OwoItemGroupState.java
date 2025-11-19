package io.wispforest.owo.itemgroup.base;

import io.wispforest.owo.Owo;
import io.wispforest.owo.itemgroup.core.CondensedEntry;
import io.wispforest.owo.itemgroup.impl.OwoItemGroupStateImpl;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

///
/// The state for the given [OwoItemGroup] with the currently selected
/// tabs and acts as the primary access for the given entries of the
/// group
///
public interface OwoItemGroupState extends ItemGroup.EntryCollector {

    @Nullable
    static OwoItemGroupState get(ItemGroup group) {
        return get(group, null);
    }

    @Nullable
    static OwoItemGroupState get(ItemGroup group, @Nullable ItemGroup.DisplayContext context) {
        var isClientSide = true;

        if (context != null) {
            var server = Owo.currentServer();

            isClientSide = server == null || server.getRegistryManager() != context.lookup();
        }

        return get(group, isClientSide);
    }

    static OwoItemGroupState get(ItemGroup group, boolean isClientSide) {
        return OwoItemGroupStateImpl.getState(group, isClientSide);
    }

    OwoItemGroup getExtension();

    default Map<Identifier, CondensedEntry> gatherGlobalCondensedEntries(ItemGroup.DisplayContext context) {
        return gatherEntriesForAllTabs(context, (stack, visibility) -> {});
    }

    default Map<Identifier, CondensedEntry> gatherGlobalCondensedEntries(ItemGroup.DisplayContext context, IntSet activeTabs) {
        return gatherEntriesForActiveTabs(context, (stack, visibility) -> {}, activeTabs);
    }

    default Map<Identifier, CondensedEntry> gatherEntriesForAllTabs(ItemGroup.DisplayContext context, ItemGroup.Entries entries) {
        return gatherEntriesForActiveTabs(context, entries, IntSets.fromTo(0, this.getExtension().getTabs().size()));
    }

    Map<Identifier, CondensedEntry> gatherEntriesForActiveTabs(ItemGroup.DisplayContext context, ItemGroup.Entries entries, IntSet activeTabs);

    void updateSearchEntries(ItemGroup group, ItemGroup.DisplayContext context);

    /**
     * Select only {@code tab}, deselecting all other tabs,
     * using {@code context} for re-population
     */
    void selectSingleTab(int tab, ItemGroup.DisplayContext context);

    /**
     * Select {@code tab} in addition to other currently selected
     * tabs, using {@code context} for re-population.
     * <p>
     * If this group does not allow multiple selection, behaves
     * like {@link #selectSingleTab(int, ItemGroup.DisplayContext)}
     */
    void selectTab(int tab, ItemGroup.DisplayContext context);

    /**
     * Deselect {@code tab} if it is currently selected, using {@code context} for
     * re-population. If this results in no tabs being selected, all tabs are
     * automatically selected instead
     */
    void deselectTab(int tab, ItemGroup.DisplayContext context);

    /**
     * Shorthand for {@link #selectTab(int, ItemGroup.DisplayContext)} or
     * {@link #deselectTab(int, ItemGroup.DisplayContext)}, depending on the tabs
     * current state
     */
    void toggleTab(int tab, ItemGroup.DisplayContext context);

    /**
     * @return A set containing the indices of all currently
     * selected tabs
     */
    IntSet selectedTabs();

    /**
     * @return {@code true} if {@code tab} is currently selected
     */
    default boolean isTabSelected(int tab) {
        return selectedTabs().contains(tab);
    }

    Text getDisplayName(Text baseDisplayName);


}
