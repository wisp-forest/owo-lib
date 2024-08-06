package io.wispforest.owo.util.pond;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

public interface OwoMappedRegistryExtensions<T> {

    @ApiStatus.Internal
    Holder.Reference<T> owo$set(int id, ResourceKey<T> arg, T object, RegistrationInfo arg2);
}
