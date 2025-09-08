package io.wispforest.owo.itemgroup.base;

import io.wispforest.owo.itemgroup.impl.OwoItemGroupStateImpl;
import io.wispforest.owo.mixin.itemgroup.CreativeInventoryScreenAccessor;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public interface OwoItemGroupState extends ItemGroup.EntryCollector {

    @Nullable
    static OwoItemGroupState getState(ItemGroup group) {
        return getState(group, null);
    }

    static OwoItemGroupState getState(ItemGroup group, @Nullable ItemGroup.DisplayContext context) {
        return OwoItemGroupStateImpl.getState(group, context);
    }

    OwoItemGroup getExtension();

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

    default Collection<ScreenRect> getExclusionZones(int x, int y) {
        var extension = OwoItemGroup.getExtension(CreativeInventoryScreenAccessor.owo$getSelectedTab());
        if (extension == null) return Collections.emptySet();
        if (extension.getButtons().isEmpty()) return Collections.emptySet();

        int stackHeight = extension.buttonStackHeight();
        y -= 13 * (stackHeight - 4);

        final var rectangles = new ArrayList<ScreenRect>();

        for (int i = 0; i < extension.getButtons().size(); i++) {
            int xOffset = x + 198 + (i / stackHeight) * 26;
            int yOffset = y + 10 + (i % stackHeight) * 30;
            rectangles.add(new ScreenRect(xOffset, yOffset, 24, 24));
        }

        return rectangles;
    }
}
