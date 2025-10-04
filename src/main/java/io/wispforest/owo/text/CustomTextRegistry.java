package io.wispforest.owo.text;

import com.mojang.serialization.MapCodec;
import net.minecraft.text.TextContent;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public final class CustomTextRegistry {

    private static final Map<String, MapCodec<? extends TextContent>> TYPES = new HashMap<>();
    private static Codecs.IdMapper<String, MapCodec<? extends TextContent>> codecIdMapper;

    private CustomTextRegistry() {}

    public static void register(String triggerField, MapCodec<? extends TextContent> codec) {
        TYPES.put(triggerField, codec);
        if (codecIdMapper != null) {
            codecIdMapper.put(triggerField, codec);
        }
    }

    @ApiStatus.Internal
    public static void inject(Codecs.IdMapper<String, MapCodec<? extends TextContent>> mapper) {
        TYPES.forEach(mapper::put);
        codecIdMapper = mapper;
    }
}
