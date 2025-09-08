package io.wispforest.owo.itemgroup;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.format.gson.GsonDeserializer;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.itemgroup.base.ButtonDefinition;
import io.wispforest.owo.itemgroup.base.Icon;
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
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            baseGroupTab = new ItemGroupTab(
                EXTENDED_TAB_NAME,
                Icon.of(targetGroup.getIcon()),
                targetGroup.getDisplayName(),
                ((ItemGroupAccessor) targetGroup).owo$getEntryCollector()::accept,
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
}


record Tab(String name, Icon icon, TagKey<Item> tag, Identifier texture) {
    public static final StructEndec<Tab> ENDEC = StructEndecBuilder.of(
        Endec.STRING.fieldOf("name", Tab::name),
        IconEndec.ENDEC.fieldOf("icon", Tab::icon),
        MinecraftEndecs.unprefixedTagKey(RegistryKeys.ITEM).fieldOf("tag", Tab::tag),
        MinecraftEndecs.IDENTIFIER.optionalFieldOf("texture", Tab::texture, ItemGroupTab.DEFAULT_TEXTURE),
        Tab::new
    );
}

record Button(String name, String url, Icon icon) {
    public static final StructEndec<Button> ENDEC = StructEndecBuilder.of(
        Endec.STRING.fieldOf("name", Button::name),
        Endec.STRING.fieldOf("link", Button::url),
        CodecUtils.eitherEndec(Endec.STRING, IconEndec.ENDEC)
            .xmap(either -> Either.unwrap(either.mapLeft(ItemGroupButton::iconFromType)), Either::right)
            .fieldOf("icon", Button::icon),
        Button::new
    );
}

record Data(Identifier targetGroup, boolean extend, List<Tab> tabs, List<Button> buttons) {
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
            .map(tab -> new ItemGroupTab(tab.name(), tab.icon(),
                ButtonDefinition.tooltipFor(targetGroup, "tab", tab.name()), ContentSupplier.fromTag(tab.tag()), tab.texture(),
                false
            )).toList();
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