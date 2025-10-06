package io.wispforest.owo.mixin.serialization;

import com.mojang.serialization.Codec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.util.MapCarrierDecodable;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.serialization.v1.view.FabricReadView;
import net.minecraft.storage.ReadView;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(ReadView.class)
public interface ReadViewMixin extends MapCarrierDecodable, FabricReadView {
    @Shadow
    <T> Optional<T> read(String key, Codec<T> codec);

    @Override
    default <T> T getWithErrors(SerializationContext ctx, @NotNull KeyedEndec<T> key) {
        return this.read(key.key(), CodecUtils.toCodec(key.endec(), ctx))
            .orElseGet(key::defaultValue);
    }

    @Override
    default <T> boolean has(@NotNull KeyedEndec<T> key) {
        return this.contains(key.key());
    }
}
