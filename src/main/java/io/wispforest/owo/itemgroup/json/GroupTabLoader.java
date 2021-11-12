package io.wispforest.owo.itemgroup.json;

import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemExtensions;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.moddata.ModDataConsumer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Manages loading and adding JSON-based tabs to preexisting {@code ItemGroup}s
 * without needing to depend on owo
 * <p>
 * This is used instead of a {@link net.minecraft.resource.JsonDataLoader} because
 * it needs to load on the client as well
 */
@ApiStatus.Internal
public class GroupTabLoader implements ModDataConsumer {

    private static final Map<String, Pair<List<ItemGroupTab>, List<ItemGroupButton>>> CACHED_BUTTONS = new HashMap<>();

    public GroupTabLoader() {}

    public static ItemGroup onGroupCreated(String name, int index, Supplier<ItemStack> icon) {
        if (!CACHED_BUTTONS.containsKey(name)) return null;
        final var cache = CACHED_BUTTONS.remove(name);
        final var wrapperGroup = new WrapperGroup(index, name, cache.getLeft(), cache.getRight(), icon);
        wrapperGroup.initialize();
        return wrapperGroup;
    }

    @Override
    public String getDataSubdirectory() {
        return "item_group_tabs";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void acceptParsedFile(Identifier id, JsonObject json) {
        String targetGroup = JsonHelper.getString(json, "target_group");

        var tabsArray = JsonHelper.getArray(json, "tabs", new JsonArray());
        var buttonsArray = JsonHelper.getArray(json, "buttons", new JsonArray());
        var createdTabs = new ArrayList<ItemGroupTab>();
        var createdButtons = new ArrayList<ItemGroupButton>();

        tabsArray.forEach(jsonElement -> {
            if (!jsonElement.isJsonObject()) return;
            var tabObject = jsonElement.getAsJsonObject();

            var texture = new Identifier(JsonHelper.getString(tabObject, "texture", ItemGroupTab.DEFAULT_TEXTURE.toString()));

            var tag = TagFactory.ITEM.create(new Identifier(JsonHelper.getString(tabObject, "tag")));
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

        for (ItemGroup group : ItemGroup.GROUPS) {
            if (!group.getName().equals(targetGroup)) continue;
            final var wrappedGroup = new WrapperGroup(group.getIndex(), group.getName(), createdTabs, createdButtons, group::createIcon);
            wrappedGroup.initialize();

            for (var item : Registry.ITEM) {
                if (item.getGroup() != group) continue;
                ((OwoItemExtensions) item).setItemGroup(wrappedGroup);
            }

            return;
        }

        CACHED_BUTTONS.put(targetGroup, new Pair<>(createdTabs, createdButtons));
    }

}
