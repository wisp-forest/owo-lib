package owolib.extensions.io.wispforest.endec.StructEndec;

import com.mojang.serialization.MapCodec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.owo.serialization.CodecUtils;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import io.wispforest.endec.StructEndec;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Extension
public final class StructEndecExtensions {
    public static <T> MapCodec<T> toMapCodec(@This StructEndec<T> thiz, SerializationContext assumedContext) {
        return CodecUtils.toMapCodec(thiz, assumedContext);
    }

    public static <T> MapCodec<T> toMapCodec(@This StructEndec<T> thiz) {
        return CodecUtils.toMapCodec(thiz);
    }
}