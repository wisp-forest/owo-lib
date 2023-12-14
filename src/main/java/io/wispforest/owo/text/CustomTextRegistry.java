package io.wispforest.owo.text;

import net.minecraft.text.TextContent;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public final class CustomTextRegistry {

    private static final Map<String, Entry<?>> TYPES = new HashMap<>();

    private CustomTextRegistry() {}

    public static void register(TextContent.Type<?> type, String triggerField) {
        TYPES.put(type.id(), new Entry<>(triggerField, type));
    }

    @ApiStatus.Internal
    public static Map<String, Entry<?>> typesMap() {
        return TYPES;
    }

    public record Entry<C extends TextContent>(String triggerField, TextContent.Type<C> type) {}
}
