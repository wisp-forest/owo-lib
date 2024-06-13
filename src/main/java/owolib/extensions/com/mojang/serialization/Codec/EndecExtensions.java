package owolib.extensions.com.mojang.serialization.Codec;

import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.CodecUtils;
import manifold.ext.rt.ForwardingExtensionMethod;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Extension
public final class EndecExtensions {
    public static <A> Endec<A> toEndec(@This Codec<A> thiz) {
        return CodecUtils.toEndec(thiz);
    }
}