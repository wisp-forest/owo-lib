package io.wispforest.owo.text;

import com.google.gson.JsonElement;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        String prefix,
        Set<Map.Entry<String, JsonElement>> entries,
        String suffix
    ) {
        var returned = new HashSet<Map.Entry<String, JsonElement>>();
        for (var entry : entries) {
            var key = entry.getKey();
            var value = entry.getValue();

            var objectMatcher = NESTED_OBJECT_PATTERN.matcher(key);
            var listMatcher = NESTED_LIST_PATTERN.matcher(key);
            if (value.isJsonObject() && objectMatcher.matches()) {
                returned.addAll(deNest(
                    prefix + objectMatcher.group(1),
                    value.getAsJsonObject().entrySet(),
                    objectMatcher.group(2) + suffix
                ));
            } else if (value.isJsonArray() && listMatcher.matches()) {
                var start = Mth.getInt(listMatcher.group(2), 1);
                var array = value.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    returned.addAll(deNest(
                        prefix + listMatcher.group(1),
                        Set.of(Map.entry(String.valueOf((start + i)), array.get(i))),
                        listMatcher.group(3) + suffix
                    ));
                }
            } else {
                returned.add(Map.entry(
                    key.isEmpty()
                        ? prefix.replaceAll(EMPTY_STRIP_PATTERN.pattern(), "") + suffix
                        : prefix + key + suffix, value
                ));
            }
        }
        return returned;
    }
}
