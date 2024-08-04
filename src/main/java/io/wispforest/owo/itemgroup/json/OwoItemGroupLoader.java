package io.wispforest.owo.itemgroup.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.moddata.ModDataConsumer;
import io.wispforest.owo.util.pond.OwoItemExtensions;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages loading and adding JSON-based tabs to preexisting {@code ItemGroup}s
 * without needing to depend on owo
 * <p>
 * This is used instead of a {@link net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener} because
 * it needs to load on the client
 */
@ApiStatus.Internal
public class OwoItemGroupLoader implements ModDataConsumer {

    public static final OwoItemGroupLoader INSTANCE = new OwoItemGroupLoader();

    private static final Map<Identifier, JsonObject> BUFFERED_GROUPS = new HashMap<>();

    private OwoItemGroupLoader() {}

    public static void onGroupCreated(CreativeModeTab group) {
        var groupId = BuiltInRegistries.CREATIVE_MODE_TAB.getId(group);

        if (!BUFFERED_GROUPS.containsKey(groupId)) return;
        INSTANCE.acceptParsedFile(groupId, BUFFERED_GROUPS.remove(groupId));
    }

    @Override
    public void acceptParsedFile(Identifier id, JsonObject json) {
        var targetGroupId = Identifier.parse(GsonHelper.getAsString(json, "target_group"));

        CreativeModeTab searchGroup = null;
        for (CreativeModeTab group : CreativeModeTabs.allTabs()) {
            if (BuiltInRegistries.CREATIVE_MODE_TAB.getId(group).equals(targetGroupId)) {
                searchGroup = group;
                break;
            }
        }

        if (searchGroup == null) {
            BUFFERED_GROUPS.put(targetGroupId, json);
            return;
        }

        final var targetGroup = searchGroup;

        var tabsArray = GsonHelper.getAsJsonArray(json, "tabs", new JsonArray());
        var tabs = new ArrayList<ItemGroupTab>();

        tabsArray.forEach(jsonElement -> {
            if (!jsonElement.isJsonObject()) return;
            var tabObject = jsonElement.getAsJsonObject();

            var texture = Identifier.parse(GsonHelper.getAsString(tabObject, "texture", ItemGroupTab.DEFAULT_TEXTURE.toString()));

            var tag = TagKey.of(Registries.ITEM, Identifier.parse(GsonHelper.getAsString(tabObject, "tag")));
            var icon = BuiltInRegistries.ITEM.get(Identifier.parse(GsonHelper.getAsString(tabObject, "icon")));
            var name = GsonHelper.getAsString(tabObject, "name");

            tabs.add(new ItemGroupTab(
                    Icon.of(icon),
                    OwoItemGroup.ButtonDefinition.tooltipFor(targetGroup, "tab", name),
                    (context, entries) -> BuiltInRegistries.ITEM.stream().filter(item -> item.builtInRegistryHolder().isIn(tag)).forEach(entries::accept),
                    texture,
                    false
            ));
        });

        var buttonsArray = GsonHelper.getAsJsonArray(json, "buttons", new JsonArray());
        var buttons = new ArrayList<ItemGroupButton>();

        buttonsArray.forEach(jsonElement -> {
            if (!jsonElement.isJsonObject()) return;
            var buttonObject = jsonElement.getAsJsonObject();

            String link = GsonHelper.getAsString(buttonObject, "link");
            String name = GsonHelper.getAsString(buttonObject, "name");

            int u = GsonHelper.getAsInt(buttonObject, "texture_u");
            int v = GsonHelper.getAsInt(buttonObject, "texture_v");

            int textureWidth = GsonHelper.getAsInt(buttonObject, "texture_width", 64);
            int textureHeight = GsonHelper.getAsInt(buttonObject, "texture_height", 64);

            final var textureId = GsonHelper.getAsString(buttonObject, "texture", null);
            var texture = textureId == null
                    ? ItemGroupButton.ICONS_TEXTURE
                    : Identifier.parse(textureId);

            buttons.add(ItemGroupButton.link(targetGroup, Icon.of(texture, u, v, textureWidth, textureHeight), name, link));
        });

        if (targetGroup instanceof WrapperGroup wrapper) {
            wrapper.addTabs(tabs);
            wrapper.addButtons(buttons);

            if (GsonHelper.getAsBoolean(json, "extend", false)) wrapper.markExtension();
        } else {
            var wrapper = new WrapperGroup(targetGroup, targetGroupId, tabs, buttons);
            wrapper.initialize();
            if (GsonHelper.getAsBoolean(json, "extend", false)) wrapper.markExtension();

            BuiltInRegistries.ITEM.stream()
                    .filter(item -> ((OwoItemExtensions) item).owo$group() == targetGroup)
                    .forEach(item -> ((OwoItemExtensions) item).owo$setGroup(wrapper));
        }
    }

    @Override
    public String getDataSubdirectory() {
        return "item_group_tabs";
    }

    static {
        RegistryEntryAddedCallback.event(BuiltInRegistries.CREATIVE_MODE_TAB).register((rawId, id, group) -> {
            OwoItemGroupLoader.onGroupCreated(group);
        });
    }

}
