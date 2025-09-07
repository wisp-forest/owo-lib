package io.wispforest.owo.text;

import com.google.gson.JsonElement;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.Internal
public class NestedLangHandler {
    private static final Pattern NESTED_OBJECT_PATTERN = Pattern.compile("^((?:(.*?)\\.\\.)?)( ?)((?:\\.\\.(.*?))?)$");
    private static final Pattern NESTED_LIST_PATTERN = Pattern.compile("^((?:(.*?)\\.\\.)?)((?:-?[0-9]*| )?)((?:\\.\\.(.*?))?)$");
    private static final Pattern AFFIX_ESCAPE_PATTERN = Pattern.compile("^(/*)([^/]*)(/*)$");
    private static final Pattern EMPTY_STRIP_PATTERN = Pattern.compile("[^a-zA-Z0-9]+$");

    public static Set<Map.Entry<String, JsonElement>> deNest(Set<Map.Entry<String, JsonElement>> entries) {
        var denested = deNest("", entries, "");
        denested.forEach(entry -> System.out.println(entry.getKey() + " = " + entry.getValue()));
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
                    stripFromAffixes(prefix, groupOrNothing(objectMatcher, 2), ""),
                    value.getAsJsonObject().entrySet(),
                    stripFromAffixes("", groupOrNothing(objectMatcher, 5), suffix)
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
                        stripFromAffixes(prefix, groupOrNothing(listMatcher, 2), ""),
                        Set.of(Map.entry(String.valueOf((start + i)), array.get(i))),
                        stripFromAffixes("", groupOrNothing(listMatcher, 5), suffix)
                    ));
                }
            } else {
                returned.add(Map.entry(stripFromAffixes(prefix, key, suffix), value));
            }
        }
        return returned;
    }

    private static String groupOrNothing(Matcher matcher, int group) {
        return matcher.group(group) == null ? "" : matcher.group(group);
    }

    private static String stripFromAffixes(
        String prefix,
        String key,
        String suffix
    ) {
        var snipperMatcher = AFFIX_ESCAPE_PATTERN.matcher(key);
        if (!snipperMatcher.matches()) return prefix + key + suffix;
        if (key.isEmpty())  return prefix.replaceAll(EMPTY_STRIP_PATTERN.pattern(), "") + suffix;
        var leadingSnip = Math.min(snipperMatcher.group(1).length(), prefix.length());
        var trailingSnip = Math.min(snipperMatcher.group(3).length(), suffix.length());
        return prefix.substring(0, prefix.length() - leadingSnip) +
               key.substring(leadingSnip, key.length() - trailingSnip) +
               suffix.substring(trailingSnip);
    }
}
