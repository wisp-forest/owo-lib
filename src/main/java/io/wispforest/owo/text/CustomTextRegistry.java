package io.wispforest.owo.text;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public final class CustomTextRegistry {
    private static final Map<String, CustomTextContentSerializer<?>> SERIALIZERS = new HashMap<>();

    private CustomTextRegistry() {

    }

    public static void register(String baseKey, CustomTextContentSerializer<?> serializer) {
        SERIALIZERS.put(baseKey, serializer);
    }

    @ApiStatus.Internal
    public static Map<String, CustomTextContentSerializer<?>> serializerMap() {
        return SERIALIZERS;
    }
}
