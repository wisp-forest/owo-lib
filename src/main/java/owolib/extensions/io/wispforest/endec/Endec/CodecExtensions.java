package owolib.extensions.io.wispforest.endec.Endec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.owo.serialization.CodecUtils;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
@Extension
public final class CodecExtensions {

    @NotNull
    public static <T> Codec<T> toCodec(@This Endec<T> thiz) {
        return CodecUtils.toCodec(thiz);
    }

    @NotNull
    public static <T> Codec<T> toCodec(@This Endec<T> thiz, SerializationContext assumedContext) {
        return CodecUtils.toCodec(thiz, assumedContext);
    }

    @NotNull
    public static <T, B extends PacketByteBuf> PacketCodec<B, T> toPacketCodec(@This Endec<T> endec) {
        return CodecUtils.toPacketCodec(endec);
    }

    @NotNull
    @Extension
    public static <F, S> Endec<Either<F, S>> either(Endec<F> first, Endec<S> second) {
        return CodecUtils.eitherEndec(first, second);
    }

    @NotNull
    @Extension
    public static <F, S> Endec<Either<F, S>> xor(Endec<F> first, Endec<S> second) {
        return CodecUtils.xorEndec(first, second);
    }
}