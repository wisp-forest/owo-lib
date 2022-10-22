package io.wispforest.owo.itemgroup.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.moddata.ModDataConsumer;
import io.wispforest.owo.util.pond.OwoItemExtensions;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages loading and adding JSON-based tabs to preexisting {@code ItemGroup}s
 * without needing to depend on owo
 * <p>
 * This is used instead of a {@link net.minecraft.resource.JsonDataLoader} because
 * it needs to load on the client as well
 */
@ApiStatus.Internal
public class GroupTabLoader implements ModDataConsumer {

    private static final Map<Identifier, Pair<List<ItemGroupTab>, List<ItemGroupButton>>> CACHED_BUTTONS = new HashMap<>();

    public static void onGroupCreated(FabricItemGroup group) {
        if (!CACHED_BUTTONS.containsKey(group.getId())) return;

        var cache = CACHED_BUTTONS.remove(group.getId());
        var wrapperGroup = new WrapperGroup(group, cache.getLeft(), cache.getRight());
        wrapperGroup.initialize();
    }

    @Override
    public void acceptParsedFile(Identifier id, JsonObject json) {
        var targetGroup = new Identifier(JsonHelper.getString(json, "target_group"));

        var tabsArray = JsonHelper.getArray(json, "tabs", new JsonArray());
        var buttonsArray = JsonHelper.getArray(json, "buttons", new JsonArray());
        var createdTabs = new ArrayList<ItemGroupTab>();
        var createdButtons = new ArrayList<ItemGroupButton>();

        tabsArray.forEach(jsonElement -> {
            if (!jsonElement.isJsonObject()) return;
            var tabObject = jsonElement.getAsJsonObject();

            var texture = new Identifier(JsonHelper.getString(tabObject, "texture", ItemGroupTab.DEFAULT_TEXTURE.toString()));

            var tag = TagKey.of(Registry.ITEM_KEY, new Identifier(JsonHelper.getString(tabObject, "tag")));
            var icon = Registry.ITEM.get(new Identifier(JsonHelper.getString(tabObject, "icon")));
            var name = JsonHelper.getString(tabObject, "name");

            createdTabs.add(new ItemGroupTab(Icon.of(icon), name, tag, texture));
        });

        buttonsArray.forEach(jsonElement -> {
            if (!jsonElement.isJsonObject()) return;
            var buttonObject = jsonElement.getAsJsonObject();

            String link = JsonHelper.getString(buttonObject, "link");
            String name = JsonHelper.getString(buttonObject, "name");

            int u = JsonHelper.getInt(buttonObject, "texture_u");
            int v = JsonHelper.getInt(buttonObject, "texture_v");

            createdButtons.add(ItemGroupButton.link(Icon.of(ItemGroupButton.ICONS_TEXTURE, u, v, 64, 64), name, link));
        });

        for (ItemGroup group : ItemGroups.GROUPS) {
            if (!group.getId().equals(targetGroup)) continue;

            if (group instanceof WrapperGroup wrapper) {
                wrapper.addTabs(createdTabs);
                wrapper.addButtons(createdButtons);
            } else {
                final var wrappedGroup = new WrapperGroup(group, createdTabs, createdButtons);
                wrappedGroup.initialize();

                for (var item : Registry.ITEM) {
                    final var extensions = (OwoItemExtensions) item;
                    if (extensions.owo$group() != group) continue;
                    extensions.owo$setGroup(wrappedGroup);
                }
            }

            return;
        }

        CACHED_BUTTONS.put(targetGroup, new Pair<>(createdTabs, createdButtons));
    }

    @Override
    public String getDataSubdirectory() {
        return "item_group_tabs";
    }

}
