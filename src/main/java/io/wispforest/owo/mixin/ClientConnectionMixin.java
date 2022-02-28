package io.wispforest.owo.mixin;

import io.wispforest.owo.network.OwoClientConnectionExtension;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.Set;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements OwoClientConnectionExtension {
    private Set<Identifier> channels = Collections.emptySet();

    @Override
    public void owo$setChannelSet(Set<Identifier> channels) {
        this.channels = channels;
    }

    @Override
    public Set<Identifier> owo$getChannelSet() {
        return this.channels;
    }
}
