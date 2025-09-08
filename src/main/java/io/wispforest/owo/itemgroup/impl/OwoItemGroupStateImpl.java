package io.wispforest.owo.itemgroup.impl;

import io.wispforest.owo.Owo;
import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import io.wispforest.owo.mixin.itemgroup.ItemGroupAccessor;
import io.wispforest.owo.util.pond.OwoItemExtensions;
import io.wispforest.owo.util.pond.OwoItemGroupExtension;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class OwoItemGroupStateImpl implements OwoItemGroupState {

    public static final Map<EnvType, Map<RegistryKey<ItemGroup>, OwoItemGroupState>> STATES = new HashMap<>();

    private final IntSet activeTabs = new IntAVLTreeSet(IntComparators.NATURAL_COMPARATOR);
    private final IntSet activeTabsView = IntSets.unmodifiable(this.activeTabs);

    private final OwoItemGroup extension;

    public OwoItemGroupStateImpl(OwoItemGroup extension) {
        this.extension = extension;

        if (extension.allowMultiSelect()) {
            var tabs = extension.getTabs();

            for (int tabIdx = 0; tabIdx < tabs.size(); tabIdx++) {
                if (!tabs.get(tabIdx).primary()) continue;

                this.activeTabs.add(tabIdx);
            }

            if (this.activeTabs.isEmpty()) this.activeTabs.add(0);
        } else {
            this.activeTabs.add(0);
        }
    }

    @ApiStatus.Internal
    public static OwoItemGroupState getState(ItemGroup group, @Nullable ItemGroup.DisplayContext context) {
        var ext = ((OwoItemGroupExtension) group).owo$getExtension();

        if (ext == null) return null;

        var side = isClientSide(context) ? EnvType.CLIENT : EnvType.SERVER;

        var states = STATES.computeIfAbsent(side, envType -> new HashMap<>());

        return states.computeIfAbsent(ext.itemGroupId(), key -> new OwoItemGroupStateImpl(ext));
    }

    public static boolean isClientSide(ItemGroup.DisplayContext context) {
        if (context == null) return true;

        return Owo.currentServer() == null || Owo.currentServer().getRegistryManager() != context.lookup();
    }

    @Override
    public OwoItemGroup getExtension() {
        return this.extension;
    }

    public void updateEntries(ItemGroup.DisplayContext context) {
        var group = Registries.ITEM_GROUP.getOrThrow(this.extension.itemGroupId())
            .value();

        group.updateEntries(context);
    }

    @Override
    public void accept(ItemGroup.DisplayContext context, ItemGroup.Entries entries) {
        var tabs = this.extension.getTabs();

        this.activeTabs.forEach(tabIdx -> {
            tabs.get(tabIdx).contentSupplier().addItems(context, entries);
            this.collectItemsFromRegistry(entries, tabIdx);
        });
    }

    @Override
    public void updateSearchEntries(ItemGroup group, ItemGroup.DisplayContext context) {
        var searchEntries = new SearchOnlyEntries(group, context.enabledFeatures());

        this.collectItemsFromRegistry(searchEntries, -1);
        this.extension.getTabs().forEach(tab -> tab.contentSupplier().addItems(context, searchEntries));

        ((ItemGroupAccessor) group).owo$setSearchTabStacks(searchEntries.searchTabStacks);
    }

    protected void collectItemsFromRegistry(ItemGroup.Entries entries, int tab) {
        Registries.ITEM.stream()
            .filter(item -> ((OwoItemExtensions) item).owo$group() == extension.itemGroupId() && (tab < 0 || tab == ((OwoItemExtensions) item).owo$tab()))
            .forEach(item -> ((OwoItemExtensions) item).owo$stackGenerator().accept(item, entries));
    }

    @Override
    public void selectSingleTab(int tab, ItemGroup.DisplayContext context) {
        selectTab(tab, context, true);
    }

    @Override
    public void selectTab(int tab, ItemGroup.DisplayContext context) {
        selectTab(tab, context, !this.extension.allowMultiSelect());
    }

    private void selectTab(int tab, ItemGroup.DisplayContext context, boolean clearTabs) {
        if (clearTabs) this.activeTabs.clear();
        this.activeTabs.add(tab);

        this.updateEntries(context);
    }

    @Override
    public void deselectTab(int tab, ItemGroup.DisplayContext context) {
        if (!this.extension.allowMultiSelect()) return;

        this.activeTabs.remove(tab);
        if (this.activeTabs.isEmpty()) {
            for (int tabIdx = 0; tabIdx < this.extension.getTabs().size(); tabIdx++) {
                this.activeTabs.add(tabIdx);
            }
        }

        this.updateEntries(context);
    }

    @Override
    public void toggleTab(int tab, ItemGroup.DisplayContext context) {
        if (this.isTabSelected(tab)) {
            this.deselectTab(tab, context);
        } else {
            this.selectTab(tab, context);
        }
    }

    @Override
    public IntSet selectedTabs() {
        return this.activeTabsView;
    }

    @Override
    public Text getDisplayName(Text baseDisplayName) {
        if (!extension.useDynamicTitle() || activeTabsView.size() != 1) return baseDisplayName;

        var singleActiveTab = extension.getTab(selectedTabs().iterator().nextInt());

        if (singleActiveTab.primary()) return singleActiveTab.tooltip();

        return Text.translatable(
            "text.owo.itemGroup.tab_template",
            baseDisplayName,
            singleActiveTab.name()
        );
    }
}
