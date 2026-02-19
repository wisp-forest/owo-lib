package io.wispforest.owo.text;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public final class CustomTextRegistry {

    private static final Map<String, MapCodec<? extends ComponentContents>> TYPES = new HashMap<>();
    private static ExtraCodecs.LateBoundIdMapper<String, MapCodec<? extends ComponentContents>> codecIdMapper;

    private CustomTextRegistry() {}

    public static void register(String triggerField, MapCodec<? extends ComponentContents> codec) {
        TYPES.put(triggerField, codec);
        if (codecIdMapper != null) {
            codecIdMapper.put(triggerField, codec);
        }
    }

    @ApiStatus.Internal
    public static void inject(ExtraCodecs.LateBoundIdMapper<String, MapCodec<? extends ComponentContents>> mapper) {
        TYPES.forEach(mapper::put);
        codecIdMapper = mapper;
    }
}
