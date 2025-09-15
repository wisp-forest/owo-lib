package io.wispforest.owo.itemgroup.data;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.format.gson.GsonDeserializer;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.itemgroup.OwoItemGroupBuilder;
import io.wispforest.owo.itemgroup.base.ButtonDefinition;
import io.wispforest.owo.itemgroup.base.Icon;
import io.wispforest.owo.itemgroup.base.ItemStacksSupplier;
import io.wispforest.owo.itemgroup.core.*;
import io.wispforest.owo.itemgroup.impl.OwoItemGroupImpl;
import io.wispforest.owo.mixin.itemgroup.ItemGroupAccessor;
import io.wispforest.owo.moddata.ModDataConsumer;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Manages loading and adding JSON-based tabs to preexisting {@code ItemGroup}s
 * without needing to depend on owo
 * <p>
 * This is used instead of a {@link JsonDataLoader} because
 * it needs to load on the client
 */
@ApiStatus.Internal
public class OwoItemGroupLoader implements ModDataConsumer {

    public static final String EXTENDED_TAB_NAME = "extended_base_tab";

    public static final OwoItemGroupLoader INSTANCE = new OwoItemGroupLoader();

    private static final Map<Identifier, Data> BUFFERED_GROUPS = new HashMap<>();

    private OwoItemGroupLoader() {}

    public static void onGroupCreated(ItemGroup group) {
        var groupId = Registries.ITEM_GROUP.getId(group);

        if (!BUFFERED_GROUPS.containsKey(groupId)) return;

        INSTANCE.handleData(group, BUFFERED_GROUPS.remove(groupId));
    }

    //--

    @Override
    public void acceptParsedFile(Identifier id, JsonObject json) {
        try {
            var data = Data.ENDEC.decodeFully(GsonDeserializer::of, json);

            if (data.tabs().isEmpty() && data.buttons().isEmpty()) return;

            var searchGroup = ItemGroups.getGroups()
                .stream()
                .filter(group -> data.targetGroup().equals(Registries.ITEM_GROUP.getId(group)))
                .findFirst();

            searchGroup.ifPresentOrElse(
                group -> handleData(group, data),
                () -> BUFFERED_GROUPS.put(data.targetGroup(), data)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Unable to handle OwoItemGroup [" + id + "] data due to a exception!", e);
        }
    }

    @Override
    public String getDataSubdirectory() {
        return "item_group_tabs";
    }

    //--

    public void handleData(ItemGroup targetGroup, Data data) {
        final ItemGroupTab baseGroupTab;

        if (data.extend()) {
            var collector = ((ItemGroupAccessor) targetGroup).owo$getEntryCollector();

            baseGroupTab = new ItemGroupTab(
                EXTENDED_TAB_NAME,
                Icon.of(targetGroup.getIcon()),
                targetGroup.getDisplayName(),
                (context, entries) -> collector.accept(context, entries::add),
                ItemGroupTab.DEFAULT_TEXTURE,
                true
            );
        } else {
            baseGroupTab = null;
        }

        OwoItemGroupBuilder.modifyItemGroup(data.targetGroupKey(), builder -> {
            builder.initializer(ext -> {
                var extImpl = ((OwoItemGroupImpl) ext);

                if (baseGroupTab != null && !extImpl.hasTab(EXTENDED_TAB_NAME)) {
                    ((OwoItemGroupImpl) ext).addTabFirst(baseGroupTab);
                }

                ext.addTabs(data.createTabs());
                ext.addButtons(data.createButtons());
            });
        });
    }

    static {
        RegistryEntryAddedCallback.event(Registries.ITEM_GROUP).register((rawId, id, group) -> {
            OwoItemGroupLoader.onGroupCreated(group);
        });
    }

    /*
     * {
     *  "entries": [
     *    "minecraft:air",
     *    {
     *      "item": "minecraft:air"
     *    },
     *    {
     *      "items": [
     *          ...
     *      ]
     *    },
     *    {
     *      "tag": "minecraft:wools"
     *    },
     *    {
     *      "registry": "block"
     *      "entry": "minecraft:shulker_block"
     *    }
     *  ]
     * }
     */
    private record Tab(String name, Icon icon, List<RawItemStacksSupplier> suppliers, Identifier texture, boolean areTagsCondensable) {
        public static final StructEndec<Tab> ENDEC = StructEndecBuilder.of(
            Endec.STRING.fieldOf("name", Tab::name),
            Icon.ENDEC.fieldOf("icon", Tab::icon),
            CodecUtils.eitherStructEndec(RawItemStacksSupplier.LIST_ENDEC.structOf("entries"), ItemStacksSupplier.ENDEC)
                .xmap(
                    either -> mapAndUnwrap(either, supplier -> List.of(new RawItemStacksSupplier(supplier))),
                    Either::left
                ).flatFieldOf(Tab::suppliers),
            MinecraftEndecs.IDENTIFIER.optionalFieldOf("texture", Tab::texture, ItemGroupTab.DEFAULT_TEXTURE),
            Endec.BOOLEAN.optionalFieldOf("are_tags_condensable", Tab::areTagsCondensable, false),
            Tab::new
        );

        private record RawItemStacksSupplier(ItemStacksSupplier stackSupplier, @Nullable Identifier condensedId) {
            private RawItemStacksSupplier(ItemStacksSupplier stackSupplier) {
                this(stackSupplier, null);
            }

            public static final Endec<ItemStack> CONDENSED_ITEM_STACK = CodecUtils.eitherEndec(MinecraftEndecs.ITEM_STACK, MinecraftEndecs.ofRegistry(Registries.ITEM))
                .xmap(
                    either -> mapAndUnwrap(either, Item::getDefaultStack),
                    stack -> (stack.getCount() > 1 || !stack.getComponentChanges().isEmpty()) ? Either.left(stack) : Either.right(stack.getItem()));

            private static final Endec<ItemStacksSupplier> SUPPLIER_ENDEC = CodecUtils.eitherEndec(
                CONDENSED_ITEM_STACK.xmap(stack -> ItemStacksSupplier.stacks(List.of(stack)), supplier1 -> supplier1.get().getFirst()),
                ItemStacksSupplier.ENDEC
            ).xmap(
                Either::unwrap,
                supplier1 -> (supplier1 instanceof ItemStacksSupplier.StackCollection(var stacks) && stacks.size() == 1) ? Either.left(supplier1) : Either.right(supplier1));

            public static final StructEndec<RawItemStacksSupplier> BASE_ENDEC = StructEndecBuilder.of(
                ItemStacksSupplier.ENDEC.flatFieldOf(RawItemStacksSupplier::stackSupplier),
                MinecraftEndecs.IDENTIFIER.optionalFieldOf("condensed_id", RawItemStacksSupplier::condensedId, () -> null),
                RawItemStacksSupplier::new);

            public static final Endec<RawItemStacksSupplier> ENDEC = CodecUtils.eitherEndec(BASE_ENDEC, SUPPLIER_ENDEC)
                .xmap(either -> mapAndUnwrap(either, RawItemStacksSupplier::new), Either::left);

            public static final Endec<List<RawItemStacksSupplier>> LIST_ENDEC = CodecUtils.eitherEndec(RawItemStacksSupplier.ENDEC.listOf(), RawItemStacksSupplier.ENDEC)
                .xmap(either -> mapAndUnwrap(either, List::of), list -> list.size() == 1 ? Either.right(list.getFirst()) : Either.left(list));
        }
    }

    private record Button(String name, String url, Icon icon) {
        public static final StructEndec<Button> ENDEC = StructEndecBuilder.of(
            Endec.STRING.fieldOf("name", Button::name),
            Endec.STRING.fieldOf("link", Button::url),
            CodecUtils.eitherEndec(Endec.STRING, Icon.ENDEC)
                .xmap(either -> Either.unwrap(either.mapLeft(ItemGroupButton::iconFromType)), Either::right)
                .fieldOf("icon", Button::icon),
            Button::new
        );
    }

    private record Data(Identifier targetGroup, boolean extend, List<Tab> tabs, List<Button> buttons) {
        public static final StructEndec<Data> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.IDENTIFIER.fieldOf("target_group", Data::targetGroup),
            Endec.BOOLEAN.optionalFieldOf("extend", Data::extend, false),
            Tab.ENDEC.listOf().optionalFieldOf("tabs", Data::tabs, List::of),
            Button.ENDEC.listOf().optionalFieldOf("buttons", Data::buttons, List::of),
            Data::new
        );

        public List<ItemGroupTab> createTabs() {
            var targetGroup = targetGroupKey();

            return tabs.stream()
                .map(tab -> {
                    var rawSuppliers = tab.suppliers();

                    return new ItemGroupTab(
                        tab.name(),
                        tab.icon(),
                        ButtonDefinition.tooltipFor(targetGroup, "tab", tab.name()),
                        (context, entries) -> {
                            for (var rawSupplier : rawSuppliers) {
                                if (rawSupplier.condensedId() != null) {
                                    entries.addEntry(rawSupplier.condensedId(), rawSupplier.stackSupplier());
                                } else {
                                    var supplier = rawSupplier.stackSupplier();

                                    if (tab.areTagsCondensable() && supplier instanceof ItemStacksSupplier.RegistryTag(var tagKey)) {
                                        entries.addEntry(tagKey);
                                    } else {
                                        entries.addAll(supplier.get());
                                    }
                                }
                            }
                        },
                        tab.texture(),
                        false
                    );
                }).toList();
        }

        public List<ItemGroupButton> createButtons() {
            var targetGroup = targetGroupKey();

            return buttons.stream()
                .map(button -> ItemGroupButton.link(targetGroup, button.icon(), button.name(), button.url()))
                .toList();
        }

        public RegistryKey<ItemGroup> targetGroupKey() {
            return RegistryKey.of(RegistryKeys.ITEM_GROUP, targetGroup());
        }
    }

    private static <T, V> T mapAndUnwrap(Either<T, V> either, Function<V, T> mapFunc) {
        return Either.unwrap(either.mapRight(mapFunc));
    }
}