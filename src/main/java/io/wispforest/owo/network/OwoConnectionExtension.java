package io.wispforest.owo.network;

import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import net.minecraft.resources.Identifier;

@ApiStatus.Internal
public interface OwoConnectionExtension {
    void owo$setChannelSet(Set<Identifier> channels);

    Set<Identifier> owo$getChannelSet();
}
