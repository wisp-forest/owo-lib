package io.wispforest.owo.text;

import com.google.gson.JsonElement;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.Internal
public class NestedLangHandler {
    private static final Pattern NESTED_OBJECT_PATTERN = Pattern.compile("^((?:(.*?)\\.\\.)?)( ?)((?:\\.\\.(.*?))?)$");
    private static final Pattern NESTED_LIST_PATTERN = Pattern.compile("^((?:(.*?)\\.\\.)?)((?:-?[0-9]*| )?)((?:\\.\\.(.*?))?)$");

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
            if (
                    value.isJsonObject() &&
                    objectMatcher.matches() &&
                    (!groupOrNothing(objectMatcher, 1).isEmpty() || !groupOrNothing(objectMatcher, 4).isEmpty()) &&
                    (groupOrNothing(objectMatcher, 3).isEmpty() || (!groupOrNothing(objectMatcher, 2).isEmpty() && !groupOrNothing(objectMatcher, 5).isEmpty()))
            ) {
                returned.addAll(deNest(
                        prefix + groupOrNothing(objectMatcher, 2),
                        value.getAsJsonObject().entrySet(),
                        groupOrNothing(objectMatcher, 5) + suffix
                ));
            } else if (
                    value.isJsonArray() &&
                    listMatcher.matches() &&
                    (!groupOrNothing(listMatcher, 1).isEmpty() || !groupOrNothing(listMatcher, 4).isEmpty()) &&
                    (!groupOrNothing(listMatcher, 3).isEmpty() || (groupOrNothing(listMatcher, 2).isEmpty() || groupOrNothing(listMatcher, 5).isEmpty()))
            ) {
                var start = MathHelper.parseInt(groupOrNothing(listMatcher, 3), 1);
                var array = value.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    returned.addAll(deNest(
                            prefix + groupOrNothing(listMatcher, 2),
                            Set.of(Map.entry(String.valueOf((start + i)), array.get(i))),
                            groupOrNothing(listMatcher, 5) + suffix
                    ));
                }
            } else {
                returned.add(Map.entry(prefix + key + suffix, value));
            }
        }
        return returned;
    }

    private static boolean test(Matcher matcher) {
        return matcher.matches() &&
               (!groupOrNothing(matcher, 1).isEmpty() || !groupOrNothing(matcher, 4).isEmpty()) &&
               (groupOrNothing(matcher, 3).isEmpty() || (!groupOrNothing(matcher, 2).isEmpty() && !groupOrNothing(matcher, 5).isEmpty()));
    }

    private static String groupOrNothing(Matcher matcher, int group) {
        return matcher.group(group) == null ? "" : matcher.group(group);
    }
}
