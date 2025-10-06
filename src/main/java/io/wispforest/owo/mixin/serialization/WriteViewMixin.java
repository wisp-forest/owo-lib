package io.wispforest.owo.mixin.serialization;

import com.mojang.serialization.Codec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.util.MapCarrierEncodable;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.storage.WriteView;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WriteView.class)
public interface WriteViewMixin extends MapCarrierEncodable {

    @Shadow <T> void put(String key, Codec<T> codec, T value);

    @Shadow void remove(String key);

    @Override
    default <T> void put(SerializationContext ctx, @NotNull KeyedEndec<T> key, @NotNull T value) {
        this.put(key.key(), CodecUtils.toCodec(key.endec(), ctx), value);
    }

    @Override
    default <T> void delete(@NotNull KeyedEndec<T> key) {
        this.remove(key.key());
    }
}
