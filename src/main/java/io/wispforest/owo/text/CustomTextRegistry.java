package io.wispforest.owo.text;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.ComponentContents;

public final class CustomTextRegistry {

    private static final Map<String, Entry<?>> TYPES = new HashMap<>();

    private CustomTextRegistry() {}

    public static void register(ComponentContents.Type<?> type, String triggerField) {
        TYPES.put(type.id(), new Entry<>(triggerField, type));
    }

    @ApiStatus.Internal
    public static Map<String, Entry<?>> typesMap() {
        return TYPES;
    }

    public record Entry<C extends ComponentContents>(String triggerField, ComponentContents.Type<C> type) {}
}
