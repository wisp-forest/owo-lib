package io.wispforest.owo.itemgroup.impl;

import io.wispforest.owo.Owo;
import io.wispforest.owo.itemgroup.core.CondensedEntries;
import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import io.wispforest.owo.itemgroup.base.OwoItemGroupEntries;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import io.wispforest.owo.itemgroup.core.CondensedEntry;
import io.wispforest.owo.mixin.itemgroup.ItemGroupAccessor;
import io.wispforest.owo.util.pond.OwoItemExtensions;
import io.wispforest.owo.util.pond.OwoItemGroupExtension;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.fabricmc.api.EnvType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class OwoItemGroupStateImpl implements OwoItemGroupState {

    public static final Map<EnvType, Map<RegistryKey<ItemGroup>, OwoItemGroupState>> STATES = new HashMap<>();

    private final IntSet activeTabs = new IntAVLTreeSet(IntComparators.NATURAL_COMPARATOR);
    private final IntSet activeTabsView = IntSets.unmodifiable(this.activeTabs);

    private final OwoItemGroup extension;

    private final Map<Integer, Map<Identifier, CondensedEntry>> condensedEntries = new LinkedHashMap<>();

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
    public static OwoItemGroupState getState(ItemGroup group, boolean isClientSide) {
        var ext = ((OwoItemGroupExtension) group).owo$getExtension();

        if (ext == null) return null;

        var side = isClientSide ? EnvType.CLIENT : EnvType.SERVER;

        var states = STATES.computeIfAbsent(side, envType -> new HashMap<>());

        return states.computeIfAbsent(ext.itemGroupId(), key -> new OwoItemGroupStateImpl(ext));
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

    public Map<Integer, Map<Identifier, CondensedEntry>> activeCondensedEntries() {
        return this.condensedEntries;
    }

    @Override
    public void accept(ItemGroup.DisplayContext context, ItemGroup.Entries entries) {
        var activeTabs = isClientSide(context)
            ? this.activeTabs
            : IntSets.fromTo(0, this.extension.getTabs().size());

        var tabs = this.extension.getTabs();

        this.condensedEntries.clear();

        var key = this.extension.itemGroupId();

        activeTabs.forEach(tabIdx -> {
            var callback = new CondensedEntries.RegistrationCallback() {
                @Override
                public CondensedEntries.RegistrationCallback addEntry(CondensedEntry entry) {
                    condensedEntries.computeIfAbsent(tabIdx, integer -> new LinkedHashMap<>())
                        .put(entry.id(), entry);

                    return this;
                }
            };

            tabs.get(tabIdx).contentSupplier().addItems(context, new OwoItemGroupEntriesImpl(entries, key, tabIdx, callback));

            this.collectItemsFromRegistry(entries, tabIdx);
        });
    }

    public static boolean isClientSide(ItemGroup.DisplayContext context) {
        var isClientSide = true;

        if (context != null) {
            var server = Owo.currentServer();

            isClientSide = server == null || server.getRegistryManager() != context.lookup();
        }

        return isClientSide;
    }

    @Override
    public void updateSearchEntries(ItemGroup group, ItemGroup.DisplayContext context) {
        var searchEntries = new SearchOnlyEntries(group, context.enabledFeatures());

        this.collectItemsFromRegistry(searchEntries, -1);

        var tabs = this.extension.getTabs();

        var key = this.extension.itemGroupId();

        for (int i = 0; i < tabs.size(); i++) {
            var tab = tabs.get(i);

            tab.contentSupplier().addItems(context, new OwoItemGroupEntriesImpl(searchEntries, key, i, new CondensedEntries.RegistrationCallback() {
                @Override
                public CondensedEntries.RegistrationCallback addEntry(CondensedEntry entry) {
                    return this;
                }
            }));
        }

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

    private record OwoItemGroupEntriesImpl(ItemGroup.Entries entries, RegistryKey<ItemGroup> groupKey, int tab, CondensedEntries.RegistrationCallback callback) implements OwoItemGroupEntries {

        @Override
        public OwoItemGroupEntries add(ItemStack stack, ItemGroup.StackVisibility visibility) {
            entries.add(stack, visibility);

            return this;
        }

        @Override
        public OwoItemGroupEntries addEntry(CondensedEntry entry) {
            callback.addEntry(entry);

            this.addAll(entry.childrenEntries().get());

            return this;
        }

        @Override
        public OwoItemGroupEntries addEntry(TagKey<? extends ItemConvertible> tagKey) {
            return addEntry(groupKey.getValue().withSuffixedPath("/tab_" + tab + "/tag_" + tagKey.id().toUnderscoreSeparatedString()), tagKey);
        }
    }

    private static class SearchOnlyEntries extends ItemGroup.EntriesImpl {

        public SearchOnlyEntries(ItemGroup group, FeatureSet enabledFeatures) {
            super(group, enabledFeatures);
        }

        @Override
        public void add(ItemStack stack, ItemGroup.StackVisibility visibility) {
            if (visibility == ItemGroup.StackVisibility.PARENT_TAB_ONLY) return;
            super.add(stack, ItemGroup.StackVisibility.SEARCH_TAB_ONLY);
        }
    }
}
