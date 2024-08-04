package io.wispforest.owo.mixin.text;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import io.wispforest.owo.text.CustomTextRegistry;
import io.wispforest.owo.text.CustomTextRegistry.Entry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.stream.Stream;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.StringRepresentable;

@Mixin(ComponentSerialization.class)
public abstract class TextCodecsMixin {

    @ModifyVariable(method = "dispatchingCodec", at = @At(value = "STORE", ordinal = 0))
    private static <T extends StringRepresentable> Codec<T> injectCustomTextTypesExplicit(Codec<T> codec, T[] types) {
        if (!types.getClass().getComponentType().isAssignableFrom(ComponentContents.Type.class)) return codec;

        //noinspection unchecked
        var customTextTypeCodec = Codec.stringResolver(StringRepresentable::getSerializedName, s -> (T) CustomTextRegistry.typesMap().get(s).type());

        return new Codec<>() {
            @Override
            public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
                var vanillaResult = codec.decode(ops, input);
                if (vanillaResult.result().isPresent()) return vanillaResult;

                return customTextTypeCodec.decode(ops, input);
            }

            @Override
            public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix) {
                var vanillaResult = codec.encode(input, ops, prefix);
                if (vanillaResult.result().isPresent()) return vanillaResult;

                return customTextTypeCodec.encode(input, ops, prefix);
            }
        };
    }

    @ModifyVariable(method = "dispatchingCodec", at = @At(value = "STORE", ordinal = 0))
    private static <T extends StringRepresentable, E> MapCodec<E> injectCustomTextTypesFuzzy(MapCodec<E> codec, T[] types) {
        if (!types.getClass().getComponentType().isAssignableFrom(ComponentContents.Type.class)) return codec;

        return new MapCodec<>() {
            @Override
            public <T1> DataResult<E> decode(DynamicOps<T1> ops, MapLike<T1> input) {
                var vanillaResult = codec.decode(ops, input);
                if (vanillaResult.result().isPresent()) return vanillaResult;

                for (var entry : CustomTextRegistry.typesMap().values()) {
                    if (input.get(entry.triggerField()) == null) continue;
                    return (DataResult<E>) entry.type().codec().decode(ops, input);
                }

                return vanillaResult;
            }

            @Override
            public <T1> RecordBuilder<T1> encode(E input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
                return codec.encode(input, ops, prefix);
            }

            @Override
            public <T1> Stream<T1> keys(DynamicOps<T1> ops) {
                return Stream.concat(codec.keys(ops), CustomTextRegistry.typesMap().values().stream().flatMap(entry -> entry.type().codec().keys(ops)));
            }
        };
    }

}

