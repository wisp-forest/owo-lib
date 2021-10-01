package com.glisco.owo.itemgroup.json;

import com.glisco.owo.itemgroup.Icon;
import com.glisco.owo.itemgroup.OwoItemExtensions;
import com.glisco.owo.itemgroup.gui.ItemGroupTab;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
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
public class GroupTabLoader {

    private static final Gson GSON = new Gson();
    private static final Map<String, List<ItemGroupTab>> CACHED_TABS = new HashMap<>();

    private GroupTabLoader() {}

    public static void readModGroups() {
        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            try {
                final var targetPath = modContainer.getRootPath().resolve(String.format("data/%s/item_group_tabs", modContainer.getMetadata().getId()));

                if (!Files.exists(targetPath)) return;
                Files.walk(targetPath).forEach(path -> {
                    if (!path.toString().endsWith(".json")) return;
                    try {
                        final InputStreamReader tabData = new InputStreamReader(Files.newInputStream(path));
                        loadGroups(GSON.fromJson(tabData, JsonObject.class));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static ItemGroup onGroupCreated(String name, int index, Supplier<ItemStack> icon) {
        if (!CACHED_TABS.containsKey(name)) return null;
        final var wrapperGroup = new WrapperGroup(index, name, CACHED_TABS.remove(name), icon);
        wrapperGroup.initialize();
        return wrapperGroup;
    }

    private static void loadGroups(JsonObject json) {
        String targetGroup = JsonHelper.getString(json, "target_group");

        var tabsArray = JsonHelper.getArray(json, "tabs", new JsonArray());
        var createdTabs = new ArrayList<ItemGroupTab>();

        //noinspection ConstantConditions
        tabsArray.forEach(jsonElement -> {
            if (!jsonElement.isJsonObject()) return;
            var tabObject = jsonElement.getAsJsonObject();

            var texture = new Identifier(JsonHelper.getString(tabObject, "texture", ItemGroupTab.DEFAULT_TEXTURE.toString()));

            var tag = TagFactory.ITEM.create(new Identifier(JsonHelper.getString(tabObject, "tag")));
            var icon = Registry.ITEM.get(new Identifier(JsonHelper.getString(tabObject, "icon")));
            var name = JsonHelper.getString(tabObject, "name");

            createdTabs.add(new ItemGroupTab(Icon.of(icon), name, tag, texture));
        });

        for (ItemGroup group : ItemGroup.GROUPS) {
            if (!group.getName().equals(targetGroup)) continue;
            final var wrappedGroup = new WrapperGroup(group.getIndex(), group.getName(), createdTabs, group::createIcon);
            wrappedGroup.initialize();

            for (var item : Registry.ITEM) {
                if (item.getGroup() != group) continue;
                ((OwoItemExtensions) item).setItemGroup(wrappedGroup);
            }

            return;
        }

        CACHED_TABS.put(targetGroup, createdTabs);
    }

}
