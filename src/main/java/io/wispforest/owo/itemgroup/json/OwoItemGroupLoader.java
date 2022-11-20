package io.wispforest.owo.itemgroup.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.moddata.ModDataConsumer;
import io.wispforest.owo.util.pond.OwoItemExtensions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages loading and adding JSON-based tabs to preexisting {@code ItemGroup}s
 * without needing to depend on owo
 * <p>
 * This is used instead of a {@link net.minecraft.resource.JsonDataLoader} because
 * it needs to load on the client
 */
@ApiStatus.Internal
public class OwoItemGroupLoader implements ModDataConsumer {

    public static final OwoItemGroupLoader INSTANCE = new OwoItemGroupLoader();

    private static final Map<String, JsonObject> BUFFERED_GROUPS = new HashMap<>();

    private OwoItemGroupLoader() {}

    public static ItemGroup onGroupCreated(ItemGroup group) {
        if (!BUFFERED_GROUPS.containsKey(group.getName())) return null;
        return INSTANCE.loadGroup(BUFFERED_GROUPS.remove(group.getName()));
    }

    @Override
    public void acceptParsedFile(Identifier id, JsonObject json) {
        loadGroup(json);
    }

    protected ItemGroup loadGroup(JsonObject json) {
        var targetGroupId = JsonHelper.getString(json, "target_group");

        ItemGroup searchGroup = null;
        for (ItemGroup group : ItemGroup.GROUPS) {
            if (group.getName().equals(targetGroupId)) {
                searchGroup = group;
                break;
            }
        }

        if (searchGroup == null) {
            BUFFERED_GROUPS.put(targetGroupId, json);
            return null;
        }

        final var targetGroup = searchGroup;

        var tabsArray = JsonHelper.getArray(json, "tabs", new JsonArray());
        var tabs = new ArrayList<ItemGroupTab>();

        tabsArray.forEach(jsonElement -> {
            if (!jsonElement.isJsonObject()) return;
            var tabObject = jsonElement.getAsJsonObject();

            var texture = new Identifier(JsonHelper.getString(tabObject, "texture", ItemGroupTab.DEFAULT_TEXTURE.toString()));

            var tag = TagKey.of(Registry.ITEM_KEY, new Identifier(JsonHelper.getString(tabObject, "tag")));
            var icon = Registry.ITEM.get(new Identifier(JsonHelper.getString(tabObject, "icon")));
            var name = JsonHelper.getString(tabObject, "name");

            tabs.add(new ItemGroupTab(
                    Icon.of(icon),
                    OwoItemGroup.ButtonDefinition.tooltipFor(targetGroup, "tab", name),
                    stacks -> Registry.ITEM.stream().filter(item -> item.getRegistryEntry().isIn(tag)).map(Item::getDefaultStack).forEach(stacks::add),
                    texture,
                    false
            ));
        });

        var buttonsArray = JsonHelper.getArray(json, "buttons", new JsonArray());
        var buttons = new ArrayList<ItemGroupButton>();

        buttonsArray.forEach(jsonElement -> {
            if (!jsonElement.isJsonObject()) return;
            var buttonObject = jsonElement.getAsJsonObject();

            String link = JsonHelper.getString(buttonObject, "link");
            String name = JsonHelper.getString(buttonObject, "name");

            int u = JsonHelper.getInt(buttonObject, "texture_u");
            int v = JsonHelper.getInt(buttonObject, "texture_v");

            int textureWidth = JsonHelper.getInt(buttonObject, "texture_width", 64);
            int textureHeight = JsonHelper.getInt(buttonObject, "texture_height", 64);

            final var textureId = JsonHelper.getString(buttonObject, "texture", null);
            var texture = textureId == null
                    ? ItemGroupButton.ICONS_TEXTURE
                    : new Identifier(textureId);

            buttons.add(ItemGroupButton.link(Icon.of(texture, u, v, textureWidth, textureHeight), name, link));
        });

        if (targetGroup instanceof WrapperGroup wrapper) {
            wrapper.addTabs(tabs);
            wrapper.addButtons(buttons);

            if (JsonHelper.getBoolean(json, "extend", false)) wrapper.markExtension();

            return wrapper;
        } else {
            var wrapper = new WrapperGroup(targetGroup, tabs, buttons);
            wrapper.initialize();
            if (JsonHelper.getBoolean(json, "extend", false)) wrapper.markExtension();

            Registry.ITEM.stream()
                    .filter(item -> item.getGroup() == targetGroup)
                    .forEach(item -> ((OwoItemExtensions) item).owo$setGroup(targetGroup));

            return wrapper;
        }
    }

    @Override
    public String getDataSubdirectory() {
        return "item_group_tabs";
    }

}
