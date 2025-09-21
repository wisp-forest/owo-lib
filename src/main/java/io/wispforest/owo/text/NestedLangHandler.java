package io.wispforest.owo.text;

import com.google.gson.JsonElement;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.Internal
public class NestedLangHandler {
    private static final Pattern NESTED_OBJECT_PATTERN = Pattern.compile("^(.*?)\\{}(.*?)$");
    private static final Pattern NESTED_LIST_PATTERN = Pattern.compile("^(.*?)\\{((?:-?[0-9]*)?)}(.*?)$");
    private static final Pattern EMPTY_STRIP_PATTERN = Pattern.compile("[^a-zA-Z0-9]+$");

    public static Set<Map.Entry<String, JsonElement>> deNest(Set<Map.Entry<String, JsonElement>> entries) {
        return deNest("", entries, "");
    }

    private static Set<Map.Entry<String, JsonElement>> deNest(
        @NotNull String prefix,
        @NotNull Set<Map.Entry<String, JsonElement>> entries,
        @NotNull String suffix
    ) {
        var returned = new HashSet<Map.Entry<String, JsonElement>>();
        for (var entry : entries) {
            var key = entry.getKey();
            var value = entry.getValue();

            var objectMatcher = NESTED_OBJECT_PATTERN.matcher(key);
            var listMatcher = NESTED_LIST_PATTERN.matcher(key);
            if (value.isJsonObject() && objectMatcher.matches()) {
                returned.addAll(deNest(
                    finalizeKey(prefix, groupOrNothing(objectMatcher, 1), ""),
                    value.getAsJsonObject().entrySet(),
                    finalizeKey("", groupOrNothing(objectMatcher, 2), suffix)
                ));
            } else if (value.isJsonArray() && listMatcher.matches()) {
                var start = MathHelper.parseInt(groupOrNothing(listMatcher, 2), 1);
                var array = value.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    returned.addAll(deNest(
                        finalizeKey(prefix, groupOrNothing(listMatcher, 1), ""),
                        Set.of(Map.entry(String.valueOf((start + i)), array.get(i))),
                        finalizeKey("", groupOrNothing(listMatcher, 3), suffix)
                    ));
                }
            } else {
                returned.add(Map.entry(finalizeKey(prefix, key, suffix), value));
            }
        }
        return returned;
    }

    private static String groupOrNothing(Matcher matcher, int group) {
        return matcher.group(group) == null ? "" : matcher.group(group);
    }

    private static String finalizeKey(String prefix, String key, String suffix) {
        return key.isEmpty()
            ? prefix.replaceAll(EMPTY_STRIP_PATTERN.pattern(), "") + suffix
            : prefix + key + suffix;
    }
}
