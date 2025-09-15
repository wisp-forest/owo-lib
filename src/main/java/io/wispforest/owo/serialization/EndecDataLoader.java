package io.wispforest.owo.serialization;

import com.mojang.serialization.JsonOps;
import io.wispforest.endec.Endec;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;

///
/// An implementation of [JsonDataLoader] but designed for use with a [Endec]
/// combined with access to primary constructor when not using a [Registry] object
/// for server loading
///
public abstract class EndecDataLoader<T> extends JsonDataLoader<T> implements IdentifiableResourceReloadListener {

    protected final Endec<T> endec;

    protected EndecDataLoader(RegistryWrapper.WrapperLookup registries, Endec<T> endec, RegistryKey<? extends Registry<T>> registryRef) {
        super(registries, CodecUtils.toCodec(endec), registryRef);

        this.endec = endec;
    }

    protected EndecDataLoader(RegistryWrapper.WrapperLookup registries, Endec<T> endec, ResourceFinder finder) {
        super(registries.getOps(JsonOps.INSTANCE), CodecUtils.toCodec(endec), finder);

        this.endec = endec;
    }

    protected EndecDataLoader(Endec<T> endec, ResourceFinder finder) {
        super(CodecUtils.toCodec(endec), finder);

        this.endec = endec;
    }
}
