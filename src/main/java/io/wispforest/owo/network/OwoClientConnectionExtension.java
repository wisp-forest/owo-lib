package io.wispforest.owo.network;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

@ApiStatus.Internal
public interface OwoClientConnectionExtension {
    void owo$setChannelSet(Set<Identifier> channels);

    Set<Identifier> owo$getChannelSet();
}
